#!/usr/bin/env bash
# Provisiona a infraestrutura Azure do projeto Cidades ESG Inteligentes:
#   - Resource Group
#   - Azure Database for MySQL Flexible Server (bancos esg_staging e esg_prod)
#   - App Service Plan Linux B1 (compartilhado)
#   - 2 Web Apps for Containers: staging e production
#
# Pré-requisitos: Azure CLI logado (az login) na assinatura "Azure for Students".
# Uso: GITHUB_USER=<seu-usuario> ./infra/azure-setup.sh
# Os nomes gerados e a senha do banco ficam em .azure.env (NÃO versionado).
set -euo pipefail

: "${GITHUB_USER:?Informe o usuário do GitHub dono da imagem: GITHUB_USER=<usuario> $0}"
ENV_FILE="$(dirname "$0")/../.azure.env"

# Reaproveita nomes/senha de uma execução anterior (script idempotente)
if [[ -f "$ENV_FILE" ]]; then
  source "$ENV_FILE"
else
  SUFFIX=$(openssl rand -hex 2)
  cat >"$ENV_FILE" <<EOF
LOCATION=chilecentral
RG=rg-cidades-esg
SUFFIX=$SUFFIX
MYSQL_SERVER=esg-mysql-$SUFFIX
MYSQL_ADMIN=esgadmin
MYSQL_PASSWORD='$(openssl rand -hex 10)Aa1!'
PLAN=plan-cidades-esg
APP_STAGING=esg-api-$SUFFIX-staging
APP_PROD=esg-api-$SUFFIX-prod
IMAGE=ghcr.io/$(echo "$GITHUB_USER" | tr '[:upper:]' '[:lower:]')/cidades-esg-api
EOF
  chmod 600 "$ENV_FILE"
  source "$ENV_FILE"
fi

echo ">> Registrando provedores de recursos"
az provider register -n Microsoft.Web --wait
az provider register -n Microsoft.DBforMySQL --wait

echo ">> Resource Group $RG ($LOCATION)"
az group create -n "$RG" -l "$LOCATION" -o none

echo ">> MySQL Flexible Server $MYSQL_SERVER (pode levar ~10 min)"
if ! az mysql flexible-server show -g "$RG" -n "$MYSQL_SERVER" -o none 2>/dev/null; then
  az mysql flexible-server create -g "$RG" -n "$MYSQL_SERVER" -l "$LOCATION" \
    --admin-user "$MYSQL_ADMIN" --admin-password "$MYSQL_PASSWORD" \
    --tier Burstable --sku-name Standard_B1ms --storage-size 20 --version 8.0.21 \
    --public-access 0.0.0.0 --yes -o none   # 0.0.0.0 = só serviços do Azure
fi
for db in esg_staging esg_prod; do
  az mysql flexible-server db create -g "$RG" -s "$MYSQL_SERVER" -d "$db" -o none
done

echo ">> App Service Plan $PLAN (Linux B1)"
az appservice plan create -g "$RG" -n "$PLAN" -l "$LOCATION" --is-linux --sku B1 -o none

create_webapp() {
  local app=$1 ambiente=$2 db=$3 tag=$4
  echo ">> Web App $app ($ambiente)"
  if ! az webapp show -g "$RG" -n "$app" -o none 2>/dev/null; then
    # Algumas versões do Azure CLI falham num passo opcional pós-criação (leitura do FTP/publish profile);
    # por isso validamos a existência do app em seguida em vez de confiar no código de saída.
    az webapp create -g "$RG" -p "$PLAN" -n "$app" --container-image-name "$IMAGE:$tag" -o none || true
    az webapp show -g "$RG" -n "$app" -o none
  fi

  az webapp config appsettings set -g "$RG" -n "$app" -o none --settings \
    WEBSITES_PORT=8080 \
    APP_ENV="$ambiente" \
    DATABASE_URL="jdbc:mysql://$MYSQL_SERVER.mysql.database.azure.com:3306/$db?sslMode=REQUIRED&serverTimezone=UTC" \
    DATABASE_USER="$MYSQL_ADMIN" \
    DATABASE_PWD="$MYSQL_PASSWORD"

  az webapp config set -g "$RG" -n "$app" -o none \
    --always-on true --generic-configurations '{"healthCheckPath": "/actuator/health"}'

  # Habilita autenticação básica do SCM para o deploy via publish profile (GitHub Actions)
  az resource update -g "$RG" --namespace Microsoft.Web --resource-type basicPublishingCredentialsPolicies \
    --parent "sites/$app" -n scm --set properties.allow=true -o none
}

create_webapp "$APP_STAGING" staging    esg_staging staging
create_webapp "$APP_PROD"    production esg_prod    production

echo
echo "Infra pronta:"
echo "  Staging:  https://$(az webapp show -g "$RG" -n "$APP_STAGING" --query defaultHostName -o tsv)"
echo "  Produção: https://$(az webapp show -g "$RG" -n "$APP_PROD" --query defaultHostName -o tsv)"

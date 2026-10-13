# Projeto - Cidades ESG Inteligentes

API REST para cadastro e acompanhamento de **indicadores ESG** (Ambientais, Sociais e de Governança) de cidades, com ciclo de vida completo automatizado por práticas DevOps: containerização, orquestração, pipeline de CI/CD e deploy em dois ambientes na nuvem (**staging** e **produção**).

| Ambiente | URL | Branch |
|---|---|---|
| Staging | https://esg-api-9f1e-staging.azurewebsites.net | `develop` |
| Produção | https://esg-api-9f1e-prod.azurewebsites.net | `main` |

A rota `/` de cada ambiente informa o **ambiente** e a **versão** (SHA do commit) em execução; a documentação interativa fica em `/swagger-ui/index.html`.

### Funcionalidades da API

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Ambiente e versão em execução |
| GET/POST | `/cidades` | Lista (filtro `?uf=`) / cadastra cidades |
| GET/PUT/DELETE | `/cidades/{id}` | Consulta / atualiza / remove cidade |
| GET | `/cidades/{id}/resumo` | Resumo ESG: total de indicadores por pilar |
| GET/POST | `/cidades/{id}/indicadores` | Lista (filtro `?pilar=`) / registra indicadores |
| GET/PUT/DELETE | `/indicadores/{id}` | Consulta / atualiza / remove indicador |
| GET | `/actuator/health` | Health check (Docker, Azure e pipeline) |

---

## Como executar localmente com Docker

**Pré-requisitos:** Docker e Docker Compose.

```bash
# 1. Clonar o repositório
git clone https://github.com/nataliassilva24/cidades-esg-inteligentes.git
cd cidades-esg-inteligentes

# 2. Criar o arquivo de variáveis de ambiente a partir do modelo
cp .env.example .env        # ajuste as senhas se desejar

# 3. Construir a imagem e subir API + MySQL
docker compose up -d --build

# 4. Acompanhar o status (os dois serviços devem ficar "healthy")
docker compose ps
```

Acesse:
- API: http://localhost:8080/
- Swagger: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

Exemplo de uso:

```bash
curl -X POST http://localhost:8080/cidades -H "Content-Type: application/json" \
  -d '{"nome":"Curitiba","uf":"PR","populacao":1773000}'

curl -X POST http://localhost:8080/cidades/1/indicadores -H "Content-Type: application/json" \
  -d '{"pilar":"AMBIENTAL","nome":"Coleta seletiva","valor":42.5,"unidade":"%","dataReferencia":"2025-12-31"}'

curl http://localhost:8080/cidades/1/resumo
```

Para parar: `docker compose down` (os dados do MySQL permanecem no volume `mysql_data`; use `docker compose down -v` para apagá-los).

Para rodar só os testes (sem Docker): `mvn test`.

---

## Pipeline CI/CD

**Ferramenta:** GitHub Actions (workflows em `.github/workflows/`), com imagens publicadas no **GitHub Container Registry (GHCR)** e deploy no **Azure App Service (Web App for Containers)**.

### Fluxo de branches (Git Flow simplificado)

```
feature/*  ──PR──▶  develop  ──merge──▶  main
   │                   │                   │
   CI                CD → STAGING      CD → PRODUÇÃO (aprovação manual)
```

### Workflow `ci.yml` — Integração Contínua
Disparo: push em `feature/**` e Pull Requests para `develop`/`main`.

1. Checkout do código (`actions/checkout`)
2. Setup do Java 21 Temurin com cache Maven (`actions/setup-java`)
3. **Build + testes automatizados** (`mvn clean verify` — 11 testes de integração com JUnit 5 + MockMvc + H2)
4. Publicação do relatório de testes como artefato

### Workflow `cd.yml` — Entrega/Implantação Contínua
Disparo: push em `develop` ou `main`.

| Job | O que faz |
|---|---|
| `test` | Build e testes automatizados (o deploy só acontece se passarem) |
| `build-image` | Build da imagem Docker (Buildx, com cache) e push para o GHCR com as tags `<sha-do-commit>` e `staging`/`production` |
| `deploy-staging` | (branch `develop`) Deploy da imagem no Web App de **staging** + *smoke test* em `/actuator/health` |
| `deploy-production` | (branch `main`) Aguarda **aprovação manual** (GitHub Environment `production` com *required reviewers*), faz o deploy no Web App de **produção** + *smoke test* |

Os dados sensíveis ficam fora do código: o *publish profile* de cada Web App é um **secret** do respectivo GitHub Environment, e as credenciais do banco são **App Settings** (variáveis de ambiente) de cada Web App.

### Infraestrutura na nuvem
Provisionada por script com Azure CLI (`infra/azure-setup.sh`), região **Chile Central** (região permitida pela política da assinatura Azure for Students):

- Resource Group `rg-cidades-esg`
- Azure Database for MySQL Flexible Server (Burstable B1ms) com os bancos `esg_staging` e `esg_prod`
- App Service Plan Linux B1 compartilhado
- Web Apps `esg-api-9f1e-staging` e `esg-api-9f1e-prod`

Execução: `GITHUB_USER=<usuario-github> ./infra/azure-setup.sh` (requer `az login`). Para remover tudo ao final: `az group delete -n rg-cidades-esg`.

---

## Containerização

### Dockerfile

```dockerfile
# =========================================================
# Etapa 1 - BUILD: compila o projeto com Maven + JDK 21
# =========================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /opt/app

# Copia primeiro só o pom.xml para aproveitar o cache de camadas:
# as dependências só são baixadas de novo quando o pom.xml muda.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia o código-fonte e gera o JAR (os testes já rodam no pipeline de CI)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# =========================================================
# Etapa 2 - RUNTIME: imagem leve só com o JRE 21 (Alpine)
# =========================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app

# Usuário sem privilégios de root (boa prática de segurança)
RUN addgroup -S esg && adduser -S esg -G esg

# Copia apenas o JAR gerado na etapa de build
COPY --from=build /opt/app/target/app.jar app.jar
RUN chown esg:esg app.jar
USER esg

# Versão gravada na imagem pelo pipeline (SHA do commit)
ARG APP_VERSION=dev

# Valores padrão (sobrescritos pelo docker-compose / ambiente de deploy)
ENV APP_ENV=local \
    APP_VERSION=${APP_VERSION} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# O próprio container informa se a aplicação está saudável
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# "sh -c" permite interpolar JAVA_OPTS em tempo de execução
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Estratégias adotadas

- **Multi-stage build:** a imagem final contém só o JRE e o JAR (sem Maven nem código-fonte), ficando menor e com menos superfície de ataque.
- **Cache de dependências:** o `pom.xml` é copiado antes do código, então as dependências só são baixadas novamente quando ele muda.
- **Usuário não-root** na imagem de execução.
- **Configuração 12-factor:** nenhuma credencial na imagem; banco, ambiente e versão vêm de variáveis de ambiente (`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PWD`, `APP_ENV`, `APP_VERSION`).
- **Health check** no Dockerfile, no Compose e no Azure (`/actuator/health`).
- **Rastreabilidade:** cada imagem é marcada com o SHA do commit, que a API também expõe em `/`.

### Orquestração (`docker-compose.yml`)

| Recurso | Uso |
|---|---|
| Serviços | `api` (build do Dockerfile) e `db` (MySQL 8.0) |
| Volume | `mysql_data` — persiste os dados do banco entre reinicializações |
| Rede | `esg-net` (bridge) — a API acessa o banco pelo nome `db`; o MySQL não é exposto para fora |
| Variáveis de ambiente | lidas do `.env` (modelo em `.env.example`) |
| Dependência saudável | `depends_on: condition: service_healthy` — a API só sobe após o MySQL responder ao `mysqladmin ping` |
| Reinício | `restart: unless-stopped` |

---

## Prints do funcionamento

> As evidências estão na pasta [`docs/prints/`](docs/prints/).

| Evidência | Print |
|---|---|
| Execução local com `docker compose ps` (serviços healthy) | `docs/prints/01-compose-local.png` |
| Pipeline de CI (build + testes) | `docs/prints/02-ci.png` |
| Pipeline de CD — deploy em staging | `docs/prints/03-cd-staging.png` |
| Aprovação manual e deploy em produção | `docs/prints/04-cd-producao.png` |
| Aplicação em staging (`/` e Swagger) | `docs/prints/05-staging.png` |
| Aplicação em produção (`/` e Swagger) | `docs/prints/06-producao.png` |
| Recursos no portal Azure | `docs/prints/07-azure.png` |

---

## Tecnologias utilizadas

- **Linguagem/Framework:** Java 21, Spring Boot 3.5 (Web, Data JPA, Validation, Actuator), springdoc-openapi (Swagger)
- **Banco de dados:** MySQL 8 (local e Azure Database for MySQL Flexible Server); H2 em memória nos testes
- **Testes:** JUnit 5, Spring Boot Test, MockMvc
- **Build:** Maven
- **Containers:** Docker (multi-stage), Docker Compose
- **CI/CD:** GitHub Actions, GitHub Environments (aprovação manual), GitHub Container Registry
- **Nuvem:** Microsoft Azure — App Service (Web App for Containers, Linux B1), Azure Database for MySQL, Azure CLI

---

## Checklist de entrega

| Item | OK |
|---|---|
| Projeto compactado em .ZIP com estrutura organizada | ☐ |
| Dockerfile funcional | ☑ |
| docker-compose.yml ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☐ |
| README.md com instruções e prints | ☐ |
| Documentação técnica com evidências (PDF ou PPT) | ☐ |
| Deploy realizado nos ambientes staging e produção | ☐ |

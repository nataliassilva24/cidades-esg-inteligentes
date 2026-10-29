#!/usr/bin/env bash
# Sobe a API + MySQL com Docker Compose e executa a suíte BDD.
#
# Uso (a partir da raiz do projeto):
#   ./testes-bdd/run-tests.sh            -> executa os testes com Maven local (Java 21+)
#   ./testes-bdd/run-tests.sh --docker   -> executa os testes dentro de um container (só precisa de Docker)
#
# Variáveis opcionais: APP_PORT (porta local da API, padrão 8080)
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

APP_PORT="${APP_PORT:-8080}"
export APP_PORT

if [ ! -f .env ]; then
  echo ">> Arquivo .env não encontrado: criando a partir de .env.example"
  cp .env.example .env
fi

if [ "${1:-}" = "--docker" ]; then
  echo ">> Executando API + testes BDD em containers"
  docker compose -f docker-compose.yml -f testes-bdd/docker-compose.testes.yml up --build \
    --abort-on-container-exit --exit-code-from testes db api testes
  echo ">> Relatório: testes-bdd/target/cucumber-reports/relatorio.html"
  exit 0
fi

echo ">> Subindo API + MySQL (porta ${APP_PORT})"
docker compose up -d --build db api

echo ">> Aguardando a API ficar saudável..."
for i in $(seq 1 60); do
  if curl -fs "http://localhost:${APP_PORT}/actuator/health" > /dev/null; then
    echo ">> API pronta"
    break
  fi
  [ "$i" = 60 ] && { echo "!! API não respondeu a tempo"; docker compose logs api | tail -50; exit 1; }
  sleep 3
done

echo ">> Executando testes BDD"
cd testes-bdd
mvn -B clean test -DbaseUrl="http://localhost:${APP_PORT}"
echo ">> Relatório: testes-bdd/target/cucumber-reports/relatorio.html"

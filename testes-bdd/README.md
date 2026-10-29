# Testes Automatizados BDD — HealthTrack API (Cidades ESG Inteligentes)

[![Testes BDD e de API](https://github.com/nataliassilva24/cidades-esg-inteligentes/actions/workflows/bdd-tests.yml/badge.svg?branch=feature%2Ftestes-bdd)](https://github.com/nataliassilva24/cidades-esg-inteligentes/actions/workflows/bdd-tests.yml)

> Documentação completa (cenários Gherkin, passo a passo de configuração e evidências de execução):
> [`docs/Documentacao-Testes-BDD.pdf`](docs/Documentacao-Testes-BDD.pdf) — prints em [`docs/evidencias/`](docs/evidencias/).

Suíte de testes automatizados da **HealthTrack API** usando **BDD (Behavior Driven Development)** com
**Gherkin**, executada pelo **Cucumber**. Os testes chamam a API real (com MySQL) e validam, para
todos os endpoints:

- **status code** da resposta;
- **corpo da resposta (JSON)** — valores de campos, listas e mensagens de erro;
- **contrato** da resposta via **JSON Schema**.

## Stack

| Ferramenta | Uso |
|---|---|
| Cucumber 7 (JUnit Platform) | Execução dos cenários escritos em Gherkin (em português) |
| RestAssured 5 | Chamadas HTTP e validação de status code / corpo JSON |
| RestAssured JSON Schema Validator | Testes de contrato (JSON Schema draft-04) |
| PicoContainer | Compartilha o contexto (token, variáveis, última resposta) entre os passos |
| Docker Compose | Sobe a API + MySQL (e, opcionalmente, o runner de testes) |
| GitHub Actions | Pipeline de CI que executa a suíte a cada push/PR |

## Estrutura

```
testes-bdd/
├── Dockerfile                      # imagem que executa os testes (Maven + JDK 21)
├── docker-compose.testes.yml       # serviço "testes" na mesma rede da API
├── run-tests.sh                    # script de execução em um comando
├── pom.xml
└── src/test
    ├── java/br/com/fiap/healthtrack/bdd
    │   ├── RunCucumberTest.java    # runner da suíte (gera relatório HTML/JSON)
    │   ├── steps/ApiSteps.java     # definição dos passos Gherkin
    │   └── support/ApiContext.java # contexto do cenário: token, variáveis, massa de dados
    └── resources
        ├── features/               # cenários BDD (Gherkin)
        └── schemas/                # contratos JSON Schema
```

## Cobertura de endpoints

| Endpoint | Feature | Cenários (positivo / negativo) |
|---|---|---|
| `GET /` | 01_info_saude | 200 + contrato `info.json` |
| `GET /actuator/health` | 01_info_saude | 200 + contrato `health.json` |
| `POST /auth/register` | 02_autenticacao | 201 / 400 (e-mail duplicado, e-mail inválido, senha curta, nome curto) |
| `POST /auth/login` | 02_autenticacao | 200 / 401 (senha incorreta) |
| `GET /pacientes` | 03_pacientes | 200 paginado / 401 (sem token, token inválido) |
| `GET /pacientes/{id}` | 03_pacientes | 200 / 404 |
| `POST /pacientes` | 03_pacientes | 201 / 400 (CPF inválido, CPF duplicado) / 403 (perfil PACIENTE) |
| `PUT /pacientes/{id}` | 03_pacientes | 200 |
| `DELETE /pacientes/{id}` | 03_pacientes | 204 / 403 (perfil MEDICO) |
| `POST /prontuarios` | 04_prontuarios | 201 / 400 (sem diagnóstico) |
| `GET /prontuarios/{pacienteId}` | 04_prontuarios | 200 paginado / 404 |
| `POST /exames` | 05_exames | 201 / 400 (tipo inexistente) / 404 (paciente inexistente) |
| `GET /exames/{pacienteId}/historico` | 05_exames | 200 paginado |
| `GET /indicadores/desperdicio` | 06_indicadores_esg | 200 + regra de negócio (exame repetido aumenta o indicador) / 401 |

**Total: 32 cenários, 199 passos.**

### Relação com os pilares ESG

As tags dos cenários indicam o pilar ESG validado:

- `@esg-social` — prontuário unificado, histórico de exames e o indicador de **exames repetidos
  evitados** (redução de desperdício no sistema de saúde).
- `@esg-governanca` — controle de acesso por perfil (401/403), integridade dos dados (CPF/e-mail
  únicos) e rastreabilidade do ambiente/versão implantados.

## Pré-requisitos

- **Docker** e **Docker Compose** (obrigatório — sobe a API e o MySQL);
- **Java 21+** e **Maven 3.9+** (somente para a execução local sem container).

## Como executar

Todos os comandos são executados **na raiz do projeto** (`cidades-esg-inteligentes/`).

### Opção 1 — tudo em containers (só precisa de Docker)

```bash
./testes-bdd/run-tests.sh --docker
```

Sobe MySQL, API e o container de testes; o comando termina com o código de saída dos testes.

### Opção 2 — API em container, testes com Maven local

```bash
./testes-bdd/run-tests.sh
```

Se a porta 8080 estiver ocupada, use outra:

```bash
APP_PORT=8081 ./testes-bdd/run-tests.sh
```

### Opção 3 — contra uma API já em execução (ex.: staging)

```bash
cd testes-bdd
mvn clean test -DbaseUrl=https://url-da-api
```

### Executar só uma parte da suíte (por tag)

```bash
cd testes-bdd
mvn clean test -DbaseUrl=http://localhost:8080 -Dcucumber.filter.tags="@esg-governanca"
mvn clean test -DbaseUrl=http://localhost:8080 -Dcucumber.filter.tags="@negativo"
```

> O script cria o arquivo `.env` a partir do `.env.example` caso ele não exista.

## Solução de problemas

| Sintoma | Causa | Solução |
|---|---|---|
| API não sobe e o log mostra `Access denied for user 'healthtrack_app'` | Existe um volume do MySQL de uma execução anterior criado com outra senha | `docker compose down -v` (apaga o volume local) e execute novamente |
| `port is already allocated` na porta 8080 | Outro serviço usando a porta | `APP_PORT=8081 ./testes-bdd/run-tests.sh` |
| Cenários "a mais" ou antigos aparecem na execução | Classes compiladas antigas em `target/` | Use `mvn clean test` (o script já faz isso) |

## Relatórios

Após a execução:

- `testes-bdd/target/cucumber-reports/relatorio.html` — relatório visual do Cucumber;
- `testes-bdd/target/cucumber-reports/relatorio.json` — relatório em JSON;
- `testes-bdd/target/surefire-reports/` — relatório JUnit.

## Pipeline de CI (GitHub Actions)

O workflow `.github/workflows/bdd-tests.yml` roda a cada push em `feature/**`, `develop` e `main`,
em Pull Requests e manualmente (*workflow_dispatch*). Etapas:

1. gera um `.env` com segredos aleatórios só para a execução (não é preciso cadastrar *secrets* nem
   *environments* no GitHub);
2. sobe API + MySQL com Docker Compose e aguarda o `/actuator/health`;
3. executa `mvn clean test` na pasta `testes-bdd`;
4. publica o relatório do Cucumber como artefato (`relatorio-bdd`);
5. derruba o ambiente.

Execuções: <https://github.com/nataliassilva24/cidades-esg-inteligentes/actions/workflows/bdd-tests.yml>

## Evidências

Os prints de configuração e de execução ficam em [`docs/evidencias/`](docs/evidencias/):

| Prefixo | Conteúdo |
|---|---|
| `config-*` | passo a passo de configuração (estrutura, `.env`, `pom.xml`, runner, passos, contratos, Docker, script, workflow) |
| `01..04-*` | ambiente no ar, execução dos cenários, relatório HTML e resumo |
| `exec-*` | execução 100% Docker, resultado por cenário, filtro por tag, requisições/respostas reais e prova de falha do contrato |
| `ci-*` | pipeline no GitHub Actions: lista de execuções, resumo, etapas do job e log dos testes |

## Massa de dados

Cada execução gera dados únicos (e-mails com sufixo aleatório e **CPFs válidos gerados com dígitos
verificadores**), então a suíte pode ser executada várias vezes contra o mesmo banco sem conflito.
Os usuários de teste de cada perfil (ADMIN, MEDICO, PACIENTE) são registrados pela própria API no
início da execução. O único dado pré-existente usado é o médico semente da migração `V5`.

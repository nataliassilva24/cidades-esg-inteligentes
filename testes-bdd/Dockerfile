# Imagem que executa a suíte BDD (Cucumber + RestAssured) contra a API em execução.
# A URL da API é informada pela variável BASE_URL (padrão: serviço "api" do docker-compose).
FROM maven:3.9-eclipse-temurin-21

WORKDIR /opt/testes

# Baixa as dependências primeiro para aproveitar o cache de camadas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

ENV BASE_URL=http://api:8080

# Os relatórios ficam em /opt/testes/target/cucumber-reports (montado como volume pelo compose).
# Sem "clean": a imagem já nasce sem target/ e o diretório montado não pode ser apagado.
ENTRYPOINT ["sh", "-c", "mvn -B test -DbaseUrl=$BASE_URL"]

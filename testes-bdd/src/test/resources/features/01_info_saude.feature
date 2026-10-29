# language: pt
@info @esg-governanca
Funcionalidade: Identificação do ambiente e saúde da aplicação
  Como equipe de operação da HealthTrack
  Quero consultar o ambiente, a versão e a saúde da API
  Para garantir rastreabilidade do que está implantado em cada ambiente (governança)

  @positivo
  Cenário: Consultar ambiente e versão em execução sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "info.json"
    E o campo "aplicacao" deve ser "HealthTrack API"
    E o campo "versao" deve estar preenchido

  @positivo
  Cenário: Verificar que a aplicação está saudável
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/actuator/health"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "health.json"
    E o campo "status" deve ser "UP"

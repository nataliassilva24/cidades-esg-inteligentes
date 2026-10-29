# language: pt
@indicadores @esg-social
Funcionalidade: Indicador ESG de desperdício evitado
  Como gestor de saúde pública
  Quero acompanhar quantos exames repetidos foram evitados e a economia gerada
  Para medir o impacto social da plataforma

  @positivo
  Cenário: Consultar o indicador de exames repetidos evitados
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição GET para "/indicadores/desperdicio"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "indicador-desperdicio.json"
    E o campo "impactoESG" deve conter "Social"

  @positivo
  Cenário: Exame repetido entra no cálculo do indicador
    Dado que existe um paciente cadastrado
    E que estou autenticado como "MEDICO"
    E envio uma requisição GET para "/indicadores/desperdicio"
    E guardo o campo "examesRepetidosEvitados" da resposta como "antes"
    E envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": {pacienteId}, "tipoExame": "CARDIOLOGICO", "descricao": "Eletrocardiograma", "dataRealizacao": "2026-09-01"}
      """
    E envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": {pacienteId}, "tipoExame": "CARDIOLOGICO", "descricao": "Eletrocardiograma", "dataRealizacao": "2026-09-20"}
      """
    Quando envio uma requisição GET para "/indicadores/desperdicio"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "indicador-desperdicio.json"
    E o indicador "examesRepetidosEvitados" deve ter aumentado em relação a "antes"

  @negativo @esg-governanca
  Cenário: Indicador não é exposto sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/indicadores/desperdicio"
    Então o status da resposta deve ser 401
    E a resposta deve respeitar o contrato "erro.json"

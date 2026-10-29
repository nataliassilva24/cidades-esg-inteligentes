# language: pt
@exames @esg-social
Funcionalidade: Histórico de exames
  Como médico da HealthTrack
  Quero registrar e consultar os exames de um paciente
  Para evitar a repetição desnecessária de exames (redução de desperdício)

  Contexto:
    Dado que existe um paciente cadastrado
    E que estou autenticado como "MEDICO"

  @positivo
  Cenário: Registrar um exame para o paciente
    Quando envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": {pacienteId}, "tipoExame": "SANGUE", "descricao": "Glicemia em jejum",
       "dataRealizacao": "2026-09-10", "resultado": "92 mg/dL", "laboratorio": "Laboratório Central"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "exame.json"
    E o campo "tipoExame" deve ser "SANGUE"
    E o campo "pacienteId" deve ser "{pacienteId}"

  @positivo
  Cenário: Consultar o histórico de exames do paciente
    Dado envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": {pacienteId}, "tipoExame": "IMAGEM", "descricao": "Raio-X de tórax", "dataRealizacao": "2026-07-01"}
      """
    Quando envio uma requisição GET para "/exames/{pacienteId}/historico"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "pagina-exames.json"
    E a lista "content" não deve estar vazia
    E todos os itens de "content" devem ter o campo "pacienteId" igual a "{pacienteId}"

  @negativo
  Cenário: Rejeitar exame com tipo inexistente
    Quando envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": {pacienteId}, "tipoExame": "DNA", "descricao": "Teste", "dataRealizacao": "2026-09-10"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve conter "Valores aceitos"

  @negativo
  Cenário: Rejeitar exame para paciente inexistente
    Quando envio uma requisição POST para "/exames" com o corpo:
      """
      {"pacienteId": 999999999, "tipoExame": "URINA", "descricao": "Urina tipo 1", "dataRealizacao": "2026-09-10"}
      """
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve conter "Paciente não encontrado"

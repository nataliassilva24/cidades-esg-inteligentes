# language: pt
@prontuarios @esg-social
Funcionalidade: Prontuário eletrônico unificado
  Como médico da HealthTrack
  Quero registrar e consultar atendimentos de um paciente
  Para que o histórico clínico fique disponível para qualquer unidade de saúde

  Contexto:
    Dado que existe um paciente cadastrado
    E que existe um médico cadastrado

  @positivo
  Cenário: Registrar um atendimento no prontuário do paciente
    Dado que estou autenticado como "MEDICO"
    Quando envio uma requisição POST para "/prontuarios" com o corpo:
      """
      {"pacienteId": {pacienteId}, "medicoId": {medicoId}, "dataAtendimento": "2026-09-01",
       "diagnostico": "Rinite alérgica", "prescricao": "Loratadina 10mg", "observacoes": "Retorno em 30 dias"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "prontuario.json"
    E o campo "pacienteId" deve ser "{pacienteId}"
    E o campo "diagnostico" deve ser "Rinite alérgica"

  @positivo
  Cenário: Consultar o histórico clínico do paciente
    Dado que estou autenticado como "MEDICO"
    E envio uma requisição POST para "/prontuarios" com o corpo:
      """
      {"pacienteId": {pacienteId}, "medicoId": {medicoId}, "dataAtendimento": "2026-08-15",
       "diagnostico": "Check-up anual"}
      """
    Quando envio uma requisição GET para "/prontuarios/{pacienteId}"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "pagina-prontuarios.json"
    E a lista "content" não deve estar vazia
    E todos os itens de "content" devem ter o campo "pacienteId" igual a "{pacienteId}"

  @negativo
  Cenário: Rejeitar atendimento sem diagnóstico
    Dado que estou autenticado como "MEDICO"
    Quando envio uma requisição POST para "/prontuarios" com o corpo:
      """
      {"pacienteId": {pacienteId}, "medicoId": {medicoId}, "dataAtendimento": "2026-09-01"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro-validacao.json"
    E a lista de erros deve apontar o campo "diagnostico"

  @negativo
  Cenário: Consultar histórico de paciente inexistente
    Dado que estou autenticado como "MEDICO"
    Quando envio uma requisição GET para "/prontuarios/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"

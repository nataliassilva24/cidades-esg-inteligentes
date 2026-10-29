# language: pt
@pacientes
Funcionalidade: Gestão de pacientes
  Como profissional de saúde da HealthTrack
  Quero cadastrar, consultar, atualizar e remover pacientes
  Para manter um prontuário unificado e evitar retrabalho no sistema de saúde

  @positivo @esg-social
  Cenário: Cadastrar um paciente com dados válidos
    Dado que estou autenticado como "MEDICO"
    E que gerei um CPF válido
    Quando envio uma requisição POST para "/pacientes" com o corpo:
      """
      {"nome": "Fernanda Costa", "cpf": "{cpfValido}", "dataNascimento": "1992-07-14",
       "telefone": "(11) 99876-5432", "email": "fernanda.{execucao}@teste.com", "endereco": "Rua Verde, 10"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "paciente.json"
    E o campo "nome" deve ser "Fernanda Costa"
    E o campo "cpf" deve ser "{cpfValido}"

  @positivo
  Cenário: Listar pacientes de forma paginada
    Dado que estou autenticado como "PACIENTE"
    Quando envio uma requisição GET para "/pacientes?page=0&size=5"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "pagina-pacientes.json"
    E o campo "size" deve ser o número 5
    E a lista "content" não deve estar vazia

  @positivo
  Cenário: Buscar um paciente pelo ID
    Dado que existe um paciente cadastrado
    E que estou autenticado como "MEDICO"
    Quando envio uma requisição GET para "/pacientes/{pacienteId}"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "paciente.json"
    E o campo "id" deve ser "{pacienteId}"

  @positivo
  Cenário: Atualizar os dados de contato de um paciente
    Dado que existe um paciente cadastrado
    E que estou autenticado como "ADMIN"
    E envio uma requisição GET para "/pacientes/{pacienteId}"
    E guardo o campo "cpf" da resposta como "cpfAtual"
    Quando envio uma requisição PUT para "/pacientes/{pacienteId}" com o corpo:
      """
      {"nome": "Paciente Atualizado", "cpf": "{cpfAtual}", "dataNascimento": "1990-05-20",
       "telefone": "(11) 90000-1111", "email": "atualizado.{execucao}@teste.com", "endereco": "Rua Nova, 200"}
      """
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "paciente.json"
    E o campo "nome" deve ser "Paciente Atualizado"
    E o campo "telefone" deve ser "(11) 90000-1111"

  @positivo @esg-governanca
  Cenário: Administrador remove um paciente sem histórico clínico
    Dado que existe um paciente cadastrado
    E que estou autenticado como "ADMIN"
    Quando envio uma requisição DELETE para "/pacientes/{pacienteId}"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve estar vazio
    Quando envio uma requisição GET para "/pacientes/{pacienteId}"
    Então o status da resposta deve ser 404

  @negativo
  Cenário: Buscar um paciente inexistente
    Dado que estou autenticado como "MEDICO"
    Quando envio uma requisição GET para "/pacientes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve conter "Paciente não encontrado"

  @negativo
  Cenário: Rejeitar cadastro com CPF inválido e data de nascimento futura
    Dado que estou autenticado como "MEDICO"
    Quando envio uma requisição POST para "/pacientes" com o corpo:
      """
      {"nome": "Gustavo Reis", "cpf": "111.111.111-11", "dataNascimento": "2999-01-01"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro-validacao.json"
    E a lista de erros deve apontar o campo "cpf"
    E a lista de erros deve apontar o campo "dataNascimento"

  @negativo @esg-governanca
  Cenário: Impedir cadastro de CPF duplicado (integridade dos dados)
    Dado que estou autenticado como "MEDICO"
    E que gerei um CPF válido
    E envio uma requisição POST para "/pacientes" com o corpo:
      """
      {"nome": "Helena Dias", "cpf": "{cpfValido}", "dataNascimento": "1980-02-02"}
      """
    Quando envio uma requisição POST para "/pacientes" com o corpo:
      """
      {"nome": "Outra Pessoa", "cpf": "{cpfValido}", "dataNascimento": "1981-03-03"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve conter "CPF já cadastrado"

  @negativo @esg-governanca
  Cenário: Paciente não pode cadastrar outros pacientes (controle de acesso)
    Dado que estou autenticado como "PACIENTE"
    E que gerei um CPF válido
    Quando envio uma requisição POST para "/pacientes" com o corpo:
      """
      {"nome": "Igor Mendes", "cpf": "{cpfValido}", "dataNascimento": "1995-09-09"}
      """
    Então o status da resposta deve ser 403
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "error" deve ser "Forbidden"

  @negativo @esg-governanca
  Cenário: Médico não pode remover pacientes (somente ADMIN)
    Dado que existe um paciente cadastrado
    E que estou autenticado como "MEDICO"
    Quando envio uma requisição DELETE para "/pacientes/{pacienteId}"
    Então o status da resposta deve ser 403
    E a resposta deve respeitar o contrato "erro.json"

  @negativo @esg-governanca
  Esquema do Cenário: Bloquear acesso aos dados de saúde sem token válido (<situacao>)
    Dado <condicao>
    Quando envio uma requisição GET para "/pacientes"
    Então o status da resposta deve ser 401
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve ser "Token ausente ou inválido"

    Exemplos:
      | situacao       | condicao                           |
      | sem token      | que não estou autenticado          |
      | token inválido | que estou usando um token inválido |

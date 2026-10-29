# language: pt
@autenticacao @esg-governanca
Funcionalidade: Autenticação e registro de usuários
  Como usuário da HealthTrack (médico, paciente ou administrador)
  Quero me registrar e fazer login
  Para acessar os dados de saúde apenas com credenciais válidas (controle de acesso)

  @positivo
  Cenário: Registrar um novo usuário com dados válidos
    Dado que não estou autenticado
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "Ana Souza", "email": "ana.{execucao}@teste.com", "senha": "senha123", "tipoUsuario": "MEDICO"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "token.json"
    E o campo "tipo" deve ser "Bearer"

  @positivo
  Cenário: Fazer login com credenciais válidas
    Dado que não estou autenticado
    E envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "Carlos Lima", "email": "carlos.{execucao}@teste.com", "senha": "senha123", "tipoUsuario": "PACIENTE"}
      """
    Quando envio uma requisição POST para "/auth/login" com o corpo:
      """
      {"email": "carlos.{execucao}@teste.com", "senha": "senha123"}
      """
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "token.json"
    E o campo "token" deve estar preenchido

  @negativo
  Cenário: Recusar login com senha incorreta
    Dado que não estou autenticado
    E envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "Beatriz Rocha", "email": "beatriz.{execucao}@teste.com", "senha": "senha123", "tipoUsuario": "PACIENTE"}
      """
    Quando envio uma requisição POST para "/auth/login" com o corpo:
      """
      {"email": "beatriz.{execucao}@teste.com", "senha": "senhaErrada"}
      """
    Então o status da resposta deve ser 401
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve ser "Credenciais inválidas"

  @negativo
  Cenário: Impedir registro com e-mail já cadastrado
    Dado que não estou autenticado
    E envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "Diego Prado", "email": "diego.{execucao}@teste.com", "senha": "senha123", "tipoUsuario": "MEDICO"}
      """
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "Diego Prado", "email": "diego.{execucao}@teste.com", "senha": "outraSenha", "tipoUsuario": "MEDICO"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro.json"
    E o campo "message" deve conter "E-mail já cadastrado"

  @negativo
  Esquema do Cenário: Rejeitar registro com dados inválidos (<motivo>)
    Dado que não estou autenticado
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      """
      {"nome": "<nome>", "email": "<email>", "senha": "<senha>", "tipoUsuario": "MEDICO"}
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro-validacao.json"
    E a lista de erros deve apontar o campo "<campo>"

    Exemplos:
      | motivo           | nome      | email                       | senha    | campo |
      | e-mail inválido  | Elisa Paz | email-sem-arroba            | senha123 | email |
      | senha curta      | Elisa Paz | elisa.{execucao}@teste.com  | 123      | senha |
      | nome muito curto | E         | elisa2.{execucao}@teste.com | senha123 | nome  |

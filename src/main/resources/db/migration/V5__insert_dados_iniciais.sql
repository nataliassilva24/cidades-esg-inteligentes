INSERT INTO usuarios (nome, email, senha, tipo_usuario, criado_em)
VALUES ('Administrador Sistema', 'admin@healthtrack.com',
        '$2a$10$3qWg31YN9VgV0QDAhh.xlOy/Q65526R/l3r88HXCscEQg8/WyyAy2',
        'ADMIN', '2024-01-01');

INSERT INTO usuarios (nome, email, senha, tipo_usuario, criado_em)
VALUES ('Dr. João Silva', 'joao.silva@healthtrack.com',
        '$2a$10$jnp08EuHNOjpIb85fh81JukGYlnEfXbaCkQKez0q30vsSBFmeD.S2',
        'MEDICO', '2024-01-01');

INSERT INTO usuarios (nome, email, senha, tipo_usuario, criado_em)
VALUES ('Maria Oliveira', 'maria.oliveira@email.com',
        '$2a$10$7XfTQ51ngqHa8uys5L0yOet7tjFJEVd5nAB/0UH8Nh7tonKZAjRuu',
        'PACIENTE', '2024-01-01');

INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email, endereco, usuario_id)
VALUES ('Maria Oliveira', '123.456.789-09', '1985-03-15',
        '(11) 98765-4321', 'maria.oliveira@email.com',
        'Rua das Flores, 100, São Paulo - SP',
        (SELECT id FROM usuarios WHERE email = 'maria.oliveira@email.com'));

INSERT INTO prontuarios (paciente_id, medico_id, data_atendimento, diagnostico, prescricao, observacoes)
VALUES ((SELECT id FROM pacientes WHERE cpf = '123.456.789-09'),
        (SELECT id FROM usuarios WHERE email = 'joao.silva@healthtrack.com'),
        '2024-03-10',
        'Hipertensão arterial leve',
        'Losartana 50mg - 1 comprimido ao dia',
        'Paciente orientada sobre dieta com baixo sódio e atividade física regular');

INSERT INTO exames (paciente_id, tipo_exame, descricao, data_realizacao, resultado, laboratorio)
VALUES ((SELECT id FROM pacientes WHERE cpf = '123.456.789-09'),
        'SANGUE', 'Hemograma Completo', '2024-03-08',
        'Hemoglobina 13.5 g/dL - dentro do padrão normal',
        'Laboratório Fleury');

INSERT INTO exames (paciente_id, tipo_exame, descricao, data_realizacao, resultado, laboratorio)
VALUES ((SELECT id FROM pacientes WHERE cpf = '123.456.789-09'),
        'SANGUE', 'Hemograma Completo', '2024-02-15',
        'Hemoglobina 13.2 g/dL - dentro do padrão normal',
        'Laboratório Delboni');

CREATE TABLE pacientes (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(150) NOT NULL,
    cpf             VARCHAR(14)  NOT NULL,
    data_nascimento DATE         NOT NULL,
    telefone        VARCHAR(20),
    email           VARCHAR(200),
    endereco        VARCHAR(300),
    usuario_id      BIGINT,
    CONSTRAINT pk_pacientes PRIMARY KEY (id),
    CONSTRAINT uq_pacientes_cpf UNIQUE (cpf),
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

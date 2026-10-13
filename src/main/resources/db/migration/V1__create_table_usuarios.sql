CREATE TABLE usuarios (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(150) NOT NULL,
    email        VARCHAR(200) NOT NULL,
    senha        VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(20)  NOT NULL,
    criado_em    DATE         NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_tipo CHECK (tipo_usuario IN ('PACIENTE', 'MEDICO', 'ADMIN'))
);

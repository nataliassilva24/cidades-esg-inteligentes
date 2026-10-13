CREATE TABLE exames (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    paciente_id     BIGINT        NOT NULL,
    tipo_exame      VARCHAR(30)   NOT NULL,
    descricao       VARCHAR(500)  NOT NULL,
    data_realizacao DATE          NOT NULL,
    resultado       VARCHAR(1000),
    laboratorio     VARCHAR(200),
    CONSTRAINT pk_exames PRIMARY KEY (id),
    CONSTRAINT fk_exames_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT ck_exames_tipo CHECK (tipo_exame IN ('SANGUE', 'IMAGEM', 'URINA', 'CARDIOLOGICO', 'OUTROS'))
);

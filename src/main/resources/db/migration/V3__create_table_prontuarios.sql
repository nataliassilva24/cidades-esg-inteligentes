CREATE TABLE prontuarios (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    paciente_id      BIGINT        NOT NULL,
    medico_id        BIGINT        NOT NULL,
    data_atendimento DATE          NOT NULL,
    diagnostico      VARCHAR(500)  NOT NULL,
    prescricao       VARCHAR(1000),
    observacoes      VARCHAR(1000),
    CONSTRAINT pk_prontuarios PRIMARY KEY (id),
    CONSTRAINT fk_prontuarios_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_prontuarios_medico   FOREIGN KEY (medico_id)   REFERENCES usuarios(id)
);

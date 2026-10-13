package br.com.fiap.healthtrack.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProntuarioRequest(

        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "ID do médico é obrigatório")
        Long medicoId,

        @NotNull(message = "Data do atendimento é obrigatória")
        LocalDate dataAtendimento,

        @NotBlank(message = "Diagnóstico é obrigatório")
        @Size(max = 500, message = "Diagnóstico deve ter no máximo 500 caracteres")
        String diagnostico,

        @Size(max = 1000, message = "Prescrição deve ter no máximo 1000 caracteres")
        String prescricao,

        @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
        String observacoes
) {}

package br.com.fiap.healthtrack.domain.dto.request;

import br.com.fiap.healthtrack.domain.enums.TipoExame;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExameRequest(

        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "Tipo do exame é obrigatório")
        TipoExame tipoExame,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "Data de realização é obrigatória")
        LocalDate dataRealizacao,

        @Size(max = 1000, message = "Resultado deve ter no máximo 1000 caracteres")
        String resultado,

        @Size(max = 200, message = "Laboratório deve ter no máximo 200 caracteres")
        String laboratorio
) {}

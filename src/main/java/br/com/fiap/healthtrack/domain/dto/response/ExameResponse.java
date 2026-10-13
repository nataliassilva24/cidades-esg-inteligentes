package br.com.fiap.healthtrack.domain.dto.response;

import br.com.fiap.healthtrack.domain.entity.Exame;
import br.com.fiap.healthtrack.domain.enums.TipoExame;

import java.time.LocalDate;

public record ExameResponse(
        Long id,
        Long pacienteId,
        String nomePaciente,
        TipoExame tipoExame,
        String descricao,
        LocalDate dataRealizacao,
        String resultado,
        String laboratorio
) {
    public static ExameResponse fromEntity(Exame e) {
        return new ExameResponse(
                e.getId(),
                e.getPaciente().getId(),
                e.getPaciente().getNome(),
                e.getTipoExame(),
                e.getDescricao(),
                e.getDataRealizacao(),
                e.getResultado(),
                e.getLaboratorio()
        );
    }
}

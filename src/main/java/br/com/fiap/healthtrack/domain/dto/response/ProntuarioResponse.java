package br.com.fiap.healthtrack.domain.dto.response;

import br.com.fiap.healthtrack.domain.entity.Prontuario;

import java.time.LocalDate;

public record ProntuarioResponse(
        Long id,
        Long pacienteId,
        String nomePaciente,
        Long medicoId,
        String nomeMedico,
        LocalDate dataAtendimento,
        String diagnostico,
        String prescricao,
        String observacoes
) {
    public static ProntuarioResponse fromEntity(Prontuario p) {
        return new ProntuarioResponse(
                p.getId(),
                p.getPaciente().getId(),
                p.getPaciente().getNome(),
                p.getMedico().getId(),
                p.getMedico().getNome(),
                p.getDataAtendimento(),
                p.getDiagnostico(),
                p.getPrescricao(),
                p.getObservacoes()
        );
    }
}

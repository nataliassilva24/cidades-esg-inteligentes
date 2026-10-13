package br.com.fiap.healthtrack.domain.dto.response;

import br.com.fiap.healthtrack.domain.entity.Paciente;

import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String email,
        String endereco
) {
    public static PacienteResponse fromEntity(Paciente p) {
        return new PacienteResponse(
                p.getId(),
                p.getNome(),
                p.getCpf(),
                p.getDataNascimento(),
                p.getTelefone(),
                p.getEmail(),
                p.getEndereco()
        );
    }
}

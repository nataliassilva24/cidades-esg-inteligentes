package br.com.fiap.healthtrack.domain.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record PacienteRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser uma data passada")
        LocalDate dataNascimento,

        @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[-\\s]?\\d{4}$",
                 message = "Telefone inválido. Use o formato (XX) XXXXX-XXXX")
        String telefone,

        @Email(message = "E-mail inválido")
        String email,

        @Size(max = 300, message = "Endereço deve ter no máximo 300 caracteres")
        String endereco
) {}

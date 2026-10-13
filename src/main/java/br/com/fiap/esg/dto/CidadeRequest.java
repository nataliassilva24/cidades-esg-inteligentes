package br.com.fiap.esg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CidadeRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @PositiveOrZero Long populacao
) {
}

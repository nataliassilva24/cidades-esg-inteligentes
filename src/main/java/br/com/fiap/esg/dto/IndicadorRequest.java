package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.Pilar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IndicadorRequest(
        @NotNull Pilar pilar,
        @NotBlank @Size(max = 120) String nome,
        @NotNull Double valor,
        @Size(max = 20) String unidade,
        @NotNull @PastOrPresent LocalDate dataReferencia
) {
}

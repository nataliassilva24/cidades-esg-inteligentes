package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.Pilar;

import java.util.Map;

public record ResumoEsgResponse(
        Long cidadeId,
        String cidade,
        long totalIndicadores,
        Map<Pilar, Long> indicadoresPorPilar
) {
}

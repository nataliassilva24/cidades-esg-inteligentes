package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.model.Pilar;

import java.time.LocalDate;

public record IndicadorResponse(
        Long id,
        Long cidadeId,
        Pilar pilar,
        String nome,
        Double valor,
        String unidade,
        LocalDate dataReferencia
) {

    public static IndicadorResponse from(IndicadorEsg indicador) {
        return new IndicadorResponse(
                indicador.getId(),
                indicador.getCidade().getId(),
                indicador.getPilar(),
                indicador.getNome(),
                indicador.getValor(),
                indicador.getUnidade(),
                indicador.getDataReferencia()
        );
    }
}

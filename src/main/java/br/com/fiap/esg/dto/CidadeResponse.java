package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.Cidade;

public record CidadeResponse(Long id, String nome, String uf, Long populacao) {

    public static CidadeResponse from(Cidade cidade) {
        return new CidadeResponse(cidade.getId(), cidade.getNome(), cidade.getUf(), cidade.getPopulacao());
    }
}

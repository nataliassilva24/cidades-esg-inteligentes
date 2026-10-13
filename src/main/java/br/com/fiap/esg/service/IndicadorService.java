package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.IndicadorRequest;
import br.com.fiap.esg.exception.RecursoNaoEncontradoException;
import br.com.fiap.esg.model.Cidade;
import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.model.Pilar;
import br.com.fiap.esg.repository.IndicadorEsgRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IndicadorService {

    private final IndicadorEsgRepository indicadorRepository;
    private final CidadeService cidadeService;

    public IndicadorService(IndicadorEsgRepository indicadorRepository, CidadeService cidadeService) {
        this.indicadorRepository = indicadorRepository;
        this.cidadeService = cidadeService;
    }

    @Transactional(readOnly = true)
    public List<IndicadorEsg> listarPorCidade(Long cidadeId, Pilar pilar) {
        cidadeService.buscar(cidadeId);
        return pilar == null
                ? indicadorRepository.findByCidadeId(cidadeId)
                : indicadorRepository.findByCidadeIdAndPilar(cidadeId, pilar);
    }

    @Transactional(readOnly = true)
    public IndicadorEsg buscar(Long id) {
        return indicadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Indicador", id));
    }

    @Transactional
    public IndicadorEsg criar(Long cidadeId, IndicadorRequest request) {
        Cidade cidade = cidadeService.buscar(cidadeId);
        IndicadorEsg indicador = new IndicadorEsg(
                cidade,
                request.pilar(),
                request.nome(),
                request.valor(),
                request.unidade(),
                request.dataReferencia()
        );
        return indicadorRepository.save(indicador);
    }

    @Transactional
    public IndicadorEsg atualizar(Long id, IndicadorRequest request) {
        IndicadorEsg indicador = buscar(id);
        indicador.setPilar(request.pilar());
        indicador.setNome(request.nome());
        indicador.setValor(request.valor());
        indicador.setUnidade(request.unidade());
        indicador.setDataReferencia(request.dataReferencia());
        return indicador;
    }

    @Transactional
    public void remover(Long id) {
        indicadorRepository.delete(buscar(id));
    }
}

package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.CidadeRequest;
import br.com.fiap.esg.dto.ResumoEsgResponse;
import br.com.fiap.esg.exception.RecursoNaoEncontradoException;
import br.com.fiap.esg.model.Cidade;
import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.model.Pilar;
import br.com.fiap.esg.repository.CidadeRepository;
import br.com.fiap.esg.repository.IndicadorEsgRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class CidadeService {

    private final CidadeRepository cidadeRepository;
    private final IndicadorEsgRepository indicadorRepository;

    public CidadeService(CidadeRepository cidadeRepository, IndicadorEsgRepository indicadorRepository) {
        this.cidadeRepository = cidadeRepository;
        this.indicadorRepository = indicadorRepository;
    }

    @Transactional(readOnly = true)
    public List<Cidade> listar(String uf) {
        return uf == null || uf.isBlank()
                ? cidadeRepository.findAll()
                : cidadeRepository.findByUfIgnoreCase(uf);
    }

    @Transactional(readOnly = true)
    public Cidade buscar(Long id) {
        return cidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade", id));
    }

    @Transactional
    public Cidade criar(CidadeRequest request) {
        Cidade cidade = new Cidade(request.nome(), request.uf().toUpperCase(), request.populacao());
        return cidadeRepository.save(cidade);
    }

    @Transactional
    public Cidade atualizar(Long id, CidadeRequest request) {
        Cidade cidade = buscar(id);
        cidade.setNome(request.nome());
        cidade.setUf(request.uf().toUpperCase());
        cidade.setPopulacao(request.populacao());
        return cidade;
    }

    @Transactional
    public void remover(Long id) {
        cidadeRepository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public ResumoEsgResponse resumo(Long id) {
        Cidade cidade = buscar(id);
        List<IndicadorEsg> indicadores = indicadorRepository.findByCidadeId(id);

        Map<Pilar, Long> porPilar = new EnumMap<>(Pilar.class);
        for (Pilar pilar : Pilar.values()) {
            porPilar.put(pilar, 0L);
        }
        indicadores.forEach(i -> porPilar.merge(i.getPilar(), 1L, Long::sum));

        return new ResumoEsgResponse(cidade.getId(), cidade.getNome(), indicadores.size(), porPilar);
    }
}

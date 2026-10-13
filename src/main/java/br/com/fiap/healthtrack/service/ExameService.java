package br.com.fiap.healthtrack.service;

import br.com.fiap.healthtrack.domain.dto.request.ExameRequest;
import br.com.fiap.healthtrack.domain.dto.response.ExameResponse;
import br.com.fiap.healthtrack.domain.entity.Exame;
import br.com.fiap.healthtrack.domain.entity.Paciente;
import br.com.fiap.healthtrack.domain.repository.ExameRepository;
import br.com.fiap.healthtrack.domain.repository.PacienteRepository;
import br.com.fiap.healthtrack.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExameService {

    private final ExameRepository exameRepository;
    private final PacienteRepository pacienteRepository;

    public ExameService(ExameRepository exameRepository, PacienteRepository pacienteRepository) {
        this.exameRepository = exameRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public Page<ExameResponse> historico(Long pacienteId, Pageable pageable) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw ResourceNotFoundException.comId("Paciente", pacienteId);
        }
        return exameRepository.findByPacienteId(pacienteId, pageable)
                .map(ExameResponse::fromEntity);
    }

    public ExameResponse registrar(ExameRequest request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> ResourceNotFoundException.comId("Paciente", request.pacienteId()));

        Exame exame = Exame.builder()
                .paciente(paciente)
                .tipoExame(request.tipoExame())
                .descricao(request.descricao())
                .dataRealizacao(request.dataRealizacao())
                .resultado(request.resultado())
                .laboratorio(request.laboratorio())
                .build();

        return ExameResponse.fromEntity(exameRepository.save(exame));
    }

    public Map<String, Object> calcularDesperdicioEvitado() {
        LocalDate umAnoAtras = LocalDate.now().minusYears(1);
        Long examesRepetidos = exameRepository.contarExamesRepetidos(umAnoAtras);
        long totalEvitado = examesRepetidos != null ? examesRepetidos : 0L;

        Map<String, Object> indicador = new HashMap<>();
        indicador.put("periodoAnalisado", umAnoAtras + " a " + LocalDate.now());
        indicador.put("examesRepetidosEvitados", totalEvitado);
        indicador.put("economiaEstimadaReais", totalEvitado * 150.0);
        indicador.put("descricao", "Exames do mesmo tipo realizados mais de uma vez pelo mesmo paciente no período");
        indicador.put("impactoESG", "Dimensão Social — Redução de desperdício no sistema de saúde público");
        return indicador;
    }
}

package br.com.fiap.healthtrack.service;

import br.com.fiap.healthtrack.domain.dto.request.ProntuarioRequest;
import br.com.fiap.healthtrack.domain.dto.response.ProntuarioResponse;
import br.com.fiap.healthtrack.domain.entity.Paciente;
import br.com.fiap.healthtrack.domain.entity.Prontuario;
import br.com.fiap.healthtrack.domain.entity.Usuario;
import br.com.fiap.healthtrack.domain.repository.PacienteRepository;
import br.com.fiap.healthtrack.domain.repository.ProntuarioRepository;
import br.com.fiap.healthtrack.domain.repository.UsuarioRepository;
import br.com.fiap.healthtrack.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ProntuarioService(ProntuarioRepository prontuarioRepository,
                             PacienteRepository pacienteRepository,
                             UsuarioRepository usuarioRepository) {
        this.prontuarioRepository = prontuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Page<ProntuarioResponse> historico(Long pacienteId, Pageable pageable) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw ResourceNotFoundException.comId("Paciente", pacienteId);
        }
        return prontuarioRepository.findByPacienteId(pacienteId, pageable)
                .map(ProntuarioResponse::fromEntity);
    }

    public ProntuarioResponse registrar(ProntuarioRequest request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> ResourceNotFoundException.comId("Paciente", request.pacienteId()));

        Usuario medico = usuarioRepository.findById(request.medicoId())
                .orElseThrow(() -> ResourceNotFoundException.comId("Médico", request.medicoId()));

        Prontuario prontuario = Prontuario.builder()
                .paciente(paciente)
                .medico(medico)
                .dataAtendimento(request.dataAtendimento())
                .diagnostico(request.diagnostico())
                .prescricao(request.prescricao())
                .observacoes(request.observacoes())
                .build();

        return ProntuarioResponse.fromEntity(prontuarioRepository.save(prontuario));
    }
}

package br.com.fiap.healthtrack.service;

import br.com.fiap.healthtrack.domain.dto.request.PacienteRequest;
import br.com.fiap.healthtrack.domain.dto.response.PacienteResponse;
import br.com.fiap.healthtrack.domain.entity.Paciente;
import br.com.fiap.healthtrack.domain.repository.PacienteRepository;
import br.com.fiap.healthtrack.exception.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public Page<PacienteResponse> listar(Pageable pageable) {
        return pacienteRepository.findAll(pageable).map(PacienteResponse::fromEntity);
    }

    public PacienteResponse buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .map(PacienteResponse::fromEntity)
                .orElseThrow(() -> ResourceNotFoundException.comId("Paciente", id));
    }

    public PacienteResponse cadastrar(PacienteRequest request) {
        if (pacienteRepository.existsByCpf(request.cpf())) {
            throw new ValidationException("CPF já cadastrado: " + request.cpf());
        }

        Paciente paciente = Paciente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .dataNascimento(request.dataNascimento())
                .telefone(request.telefone())
                .email(request.email())
                .endereco(request.endereco())
                .build();

        return PacienteResponse.fromEntity(pacienteRepository.save(paciente));
    }

    public PacienteResponse atualizar(Long id, PacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.comId("Paciente", id));

        if (!paciente.getCpf().equals(request.cpf()) && pacienteRepository.existsByCpf(request.cpf())) {
            throw new ValidationException("CPF já cadastrado para outro paciente: " + request.cpf());
        }

        paciente.setNome(request.nome());
        paciente.setCpf(request.cpf());
        paciente.setDataNascimento(request.dataNascimento());
        paciente.setTelefone(request.telefone());
        paciente.setEmail(request.email());
        paciente.setEndereco(request.endereco());

        return PacienteResponse.fromEntity(pacienteRepository.save(paciente));
    }

    public void remover(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw ResourceNotFoundException.comId("Paciente", id);
        }
        pacienteRepository.deleteById(id);
    }
}

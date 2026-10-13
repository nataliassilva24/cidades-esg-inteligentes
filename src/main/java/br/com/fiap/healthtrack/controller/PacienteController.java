package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.domain.dto.request.PacienteRequest;
import br.com.fiap.healthtrack.domain.dto.response.PacienteResponse;
import br.com.fiap.healthtrack.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pacientes", description = "Gerenciamento de pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os pacientes (paginado)")
    public ResponseEntity<Page<PacienteResponse>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo paciente")
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.cadastrar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do paciente")
    public ResponseEntity<PacienteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(pacienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover paciente (somente ADMIN)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        pacienteService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

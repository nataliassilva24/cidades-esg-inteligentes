package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.domain.dto.request.ProntuarioRequest;
import br.com.fiap.healthtrack.domain.dto.response.ProntuarioResponse;
import br.com.fiap.healthtrack.service.ProntuarioService;
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
@RequestMapping("/prontuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Prontuários", description = "Histórico clínico dos pacientes")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @GetMapping("/{pacienteId}")
    @Operation(summary = "Listar histórico clínico do paciente (paginado)")
    public ResponseEntity<Page<ProntuarioResponse>> historico(
            @PathVariable Long pacienteId,
            @PageableDefault(size = 10, sort = "dataAtendimento", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(prontuarioService.historico(pacienteId, pageable));
    }

    @PostMapping
    @Operation(summary = "Registrar novo atendimento/prontuário")
    public ResponseEntity<ProntuarioResponse> registrar(@Valid @RequestBody ProntuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prontuarioService.registrar(request));
    }
}

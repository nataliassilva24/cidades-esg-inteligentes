package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.domain.dto.request.ExameRequest;
import br.com.fiap.healthtrack.domain.dto.response.ExameResponse;
import br.com.fiap.healthtrack.service.ExameService;
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
@RequestMapping("/exames")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exames", description = "Histórico de exames dos pacientes")
public class ExameController {

    private final ExameService exameService;

    public ExameController(ExameService exameService) {
        this.exameService = exameService;
    }

    @GetMapping("/{pacienteId}/historico")
    @Operation(summary = "Listar histórico de exames do paciente (paginado)")
    public ResponseEntity<Page<ExameResponse>> historico(
            @PathVariable Long pacienteId,
            @PageableDefault(size = 10, sort = "dataRealizacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(exameService.historico(pacienteId, pageable));
    }

    @PostMapping
    @Operation(summary = "Registrar novo exame")
    public ResponseEntity<ExameResponse> registrar(@Valid @RequestBody ExameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exameService.registrar(request));
    }
}

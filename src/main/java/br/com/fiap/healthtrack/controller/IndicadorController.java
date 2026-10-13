package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.service.ExameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/indicadores")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Indicadores ESG", description = "Métricas de impacto social e sustentabilidade")
public class IndicadorController {

    private final ExameService exameService;

    public IndicadorController(ExameService exameService) {
        this.exameService = exameService;
    }

    @GetMapping("/desperdicio")
    @Operation(summary = "Indicador ESG: exames repetidos evitados no último ano")
    public ResponseEntity<Map<String, Object>> desperdicioEvitado() {
        return ResponseEntity.ok(exameService.calcularDesperdicioEvitado());
    }
}

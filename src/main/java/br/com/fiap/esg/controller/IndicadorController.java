package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.IndicadorRequest;
import br.com.fiap.esg.dto.IndicadorResponse;
import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.model.Pilar;
import br.com.fiap.esg.service.IndicadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Indicadores ESG", description = "Indicadores ambientais, sociais e de governança por cidade")
public class IndicadorController {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping("/cidades/{cidadeId}/indicadores")
    @Operation(summary = "Lista indicadores de uma cidade (filtro opcional por pilar)")
    public List<IndicadorResponse> listar(@PathVariable Long cidadeId,
                                          @RequestParam(required = false) Pilar pilar) {
        return service.listarPorCidade(cidadeId, pilar).stream().map(IndicadorResponse::from).toList();
    }

    @PostMapping("/cidades/{cidadeId}/indicadores")
    @Operation(summary = "Registra um indicador ESG para a cidade")
    public ResponseEntity<IndicadorResponse> criar(@PathVariable Long cidadeId,
                                                   @RequestBody @Valid IndicadorRequest request) {
        IndicadorEsg indicador = service.criar(cidadeId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/indicadores/{id}").buildAndExpand(indicador.getId()).toUri();
        return ResponseEntity.created(location).body(IndicadorResponse.from(indicador));
    }

    @GetMapping("/indicadores/{id}")
    @Operation(summary = "Busca um indicador por id")
    public IndicadorResponse buscar(@PathVariable Long id) {
        return IndicadorResponse.from(service.buscar(id));
    }

    @PutMapping("/indicadores/{id}")
    @Operation(summary = "Atualiza um indicador")
    public IndicadorResponse atualizar(@PathVariable Long id, @RequestBody @Valid IndicadorRequest request) {
        return IndicadorResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/indicadores/{id}")
    @Operation(summary = "Remove um indicador")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}

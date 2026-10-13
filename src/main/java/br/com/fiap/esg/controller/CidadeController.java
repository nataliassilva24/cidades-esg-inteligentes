package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.CidadeRequest;
import br.com.fiap.esg.dto.CidadeResponse;
import br.com.fiap.esg.dto.ResumoEsgResponse;
import br.com.fiap.esg.model.Cidade;
import br.com.fiap.esg.service.CidadeService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cidades")
@Tag(name = "Cidades", description = "Cadastro de cidades monitoradas")
public class CidadeController {

    private final CidadeService service;

    public CidadeController(CidadeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista cidades (filtro opcional por UF)")
    public List<CidadeResponse> listar(@RequestParam(required = false) String uf) {
        return service.listar(uf).stream().map(CidadeResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cidade por id")
    public CidadeResponse buscar(@PathVariable Long id) {
        return CidadeResponse.from(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma cidade")
    public ResponseEntity<CidadeResponse> criar(@RequestBody @Valid CidadeRequest request) {
        Cidade cidade = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cidade.getId()).toUri();
        return ResponseEntity.created(location).body(CidadeResponse.from(cidade));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cidade")
    public CidadeResponse atualizar(@PathVariable Long id, @RequestBody @Valid CidadeRequest request) {
        return CidadeResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cidade e seus indicadores")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/resumo")
    @Operation(summary = "Resumo ESG da cidade (quantidade de indicadores por pilar)")
    public ResumoEsgResponse resumo(@PathVariable Long id) {
        return service.resumo(id);
    }
}

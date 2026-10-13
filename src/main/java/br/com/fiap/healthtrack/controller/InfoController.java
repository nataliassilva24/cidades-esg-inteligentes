package br.com.fiap.healthtrack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint público que identifica o ambiente (local, staging, production) e a versão implantada.
 * Usado como evidência de deploy em cada ambiente.
 */
@RestController
@Tag(name = "Info", description = "Identificação do ambiente e da versão")
public class InfoController {

    @Value("${app.ambiente}")
    private String ambiente;

    @Value("${app.versao}")
    private String versao;

    @GetMapping("/")
    @Operation(summary = "Ambiente e versão em execução")
    public Map<String, String> info() {
        return Map.of(
                "aplicacao", "HealthTrack API",
                "ambiente", ambiente,
                "versao", versao,
                "documentacao", "/swagger-ui.html"
        );
    }
}

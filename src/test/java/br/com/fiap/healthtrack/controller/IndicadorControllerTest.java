package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import br.com.fiap.healthtrack.service.ExameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndicadorController.class)
@Import(TestSecurityConfig.class)
class IndicadorControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ExameService exameService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /indicadores/desperdicio — retorna métricas ESG")
    void desperdicioEvitado_autenticado_retorna200() throws Exception {
        when(exameService.calcularDesperdicioEvitado()).thenReturn(Map.of(
                "periodoAnalisado", "2025-05-24 a 2026-05-24",
                "examesRepetidosEvitados", 3,
                "economiaEstimadaReais", 450.0,
                "impactoESG", "Dimensão Social"
        ));

        mockMvc.perform(get("/indicadores/desperdicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examesRepetidosEvitados").value(3))
                .andExpect(jsonPath("$.economiaEstimadaReais").value(450.0));
    }

    @Test
    @DisplayName("GET /indicadores/desperdicio — sem autenticação retorna 401")
    void desperdicioEvitado_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/indicadores/desperdicio"))
                .andExpect(status().isUnauthorized());
    }
}

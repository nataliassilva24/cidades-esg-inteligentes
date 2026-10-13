package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InfoController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {"app.ambiente=staging", "app.versao=abc123"})
class InfoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("GET / deve informar ambiente e versão sem exigir autenticação")
    void deveInformarAmbienteEVersao() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao").value("HealthTrack API"))
                .andExpect(jsonPath("$.ambiente").value("staging"))
                .andExpect(jsonPath("$.versao").value("abc123"));
    }
}

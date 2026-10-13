package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.domain.dto.response.TokenResponse;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import br.com.fiap.healthtrack.service.AuthService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ─── /auth/register ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register — cadastro válido retorna 201 com token")
    void register_dadosValidos_retorna201() throws Exception {
        when(authService.register(any())).thenReturn(new TokenResponse("jwt-token", "Bearer", 86400000L));

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Ana","email":"ana@test.com",
                                 "senha":"senha123","tipoUsuario":"MEDICO"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/register — e-mail duplicado retorna 400")
    void register_emailDuplicado_retorna400() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ValidationException("E-mail já cadastrado: ana@test.com"));

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Ana","email":"ana@test.com",
                                 "senha":"senha123","tipoUsuario":"MEDICO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado: ana@test.com"));
    }

    @Test
    @DisplayName("POST /auth/register — campos obrigatórios ausentes retorna 400 com lista de erros")
    void register_camposAusentes_retorna400ComErrosDeCampo() throws Exception {
        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"X"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /auth/register — tipoUsuario inválido retorna 400 com valores aceitos")
    void register_tipoUsuarioInvalido_retorna400ComValoresAceitos() throws Exception {
        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@test.com",
                                 "senha":"senha123","tipoUsuario":"INVALIDO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("PACIENTE, MEDICO, ADMIN")));
    }

    // ─── /auth/login ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login — credenciais válidas retorna 200 com token")
    void login_credenciaisValidas_retorna200() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponse("jwt-token", "Bearer", 86400000L));

        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@test.com","senha":"senha123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login — senha errada retorna 401")
    void login_senhaErrada_retorna401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@test.com","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("POST /auth/login — body vazio retorna 400")
    void login_bodyVazio_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.domain.dto.response.PacienteResponse;
import br.com.fiap.healthtrack.exception.ResourceNotFoundException;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import br.com.fiap.healthtrack.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacienteController.class)
@Import(TestSecurityConfig.class)
class PacienteControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean PacienteService pacienteService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private static final PacienteResponse PACIENTE = new PacienteResponse(
            1L, "Maria Silva", "529.982.247-25",
            LocalDate.of(1990, 1, 1), "(11) 99999-0000", "maria@test.com", "Rua A, 1");

    // ─── GET /pacientes ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /pacientes — autenticado retorna 200 com lista paginada")
    void listar_autenticado_retorna200() throws Exception {
        when(pacienteService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(PACIENTE)));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /pacientes — sem autenticação retorna 401")
    void listar_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /pacientes/{id} ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /pacientes/{id} — encontrado retorna 200")
    void buscarPorId_encontrado_retorna200() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenReturn(PACIENTE);

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /pacientes/{id} — não encontrado retorna 404")
    void buscarPorId_naoEncontrado_retorna404() throws Exception {
        when(pacienteService.buscarPorId(9999L))
                .thenThrow(ResourceNotFoundException.comId("Paciente", 9999L));

        mockMvc.perform(get("/pacientes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado com id: 9999"));
    }

    // ─── POST /pacientes ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /pacientes — dados válidos retorna 201")
    void cadastrar_dadosValidos_retorna201() throws Exception {
        when(pacienteService.cadastrar(any())).thenReturn(PACIENTE);

        mockMvc.perform(post("/pacientes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Silva","cpf":"529.982.247-25",
                                 "dataNascimento":"1990-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /pacientes — CPF inválido retorna 400")
    void cadastrar_cpfInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/pacientes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Teste","cpf":"111.111.111-11",
                                 "dataNascimento":"1990-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("cpf"))
                .andExpect(jsonPath("$.errors[0].mensagem").value("CPF inválido"));
    }

    // ─── DELETE /pacientes/{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /pacientes/{id} — ADMIN sem dependências retorna 204")
    void deletar_comAdmin_semDependencias_retorna204() throws Exception {
        mockMvc.perform(delete("/pacientes/2").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("DELETE /pacientes/{id} — MEDICO retorna 403")
    void deletar_comMedico_retorna403() throws Exception {
        mockMvc.perform(delete("/pacientes/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /pacientes/{id} — paciente com dependências retorna 409")
    void deletar_comDependencias_retorna409() throws Exception {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(pacienteService).remover(1L);

        mockMvc.perform(delete("/pacientes/1").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("dependências")));
    }
}

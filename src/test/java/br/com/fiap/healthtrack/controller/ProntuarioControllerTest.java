package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.domain.dto.response.ProntuarioResponse;
import br.com.fiap.healthtrack.exception.ResourceNotFoundException;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import br.com.fiap.healthtrack.service.ProntuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProntuarioController.class)
@Import(TestSecurityConfig.class)
class ProntuarioControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ProntuarioService prontuarioService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private static final ProntuarioResponse PRONTUARIO = new ProntuarioResponse(
            1L, 1L, "Maria Silva", 2L, "Dr. João",
            LocalDate.of(2026, 5, 24), "Hipertensão leve", "Losartana 50mg", "Retorno em 30 dias");

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /prontuarios/{pacienteId} — retorna histórico paginado")
    void historico_pacienteExistente_retorna200() throws Exception {
        when(prontuarioService.historico(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(PRONTUARIO)));

        mockMvc.perform(get("/prontuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].diagnostico").value("Hipertensão leve"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /prontuarios/{pacienteId} — paciente inexistente retorna 404")
    void historico_pacienteInexistente_retorna404() throws Exception {
        when(prontuarioService.historico(eq(9999L), any(Pageable.class)))
                .thenThrow(ResourceNotFoundException.comId("Paciente", 9999L));

        mockMvc.perform(get("/prontuarios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /prontuarios — dados válidos retorna 201")
    void registrar_dadosValidos_retorna201() throws Exception {
        when(prontuarioService.registrar(any())).thenReturn(PRONTUARIO);

        mockMvc.perform(post("/prontuarios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":1,"medicoId":2,"dataAtendimento":"2026-05-24",
                                 "diagnostico":"Hipertensão leve","prescricao":"Losartana 50mg"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomePaciente").value("Maria Silva"));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /prontuarios — diagnóstico ausente retorna 400")
    void registrar_semDiagnostico_retorna400() throws Exception {
        mockMvc.perform(post("/prontuarios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":1,"medicoId":2,"dataAtendimento":"2026-05-24"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("diagnostico"));
    }
}

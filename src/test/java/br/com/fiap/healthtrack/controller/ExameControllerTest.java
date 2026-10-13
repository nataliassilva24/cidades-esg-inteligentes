package br.com.fiap.healthtrack.controller;

import br.com.fiap.healthtrack.config.TestSecurityConfig;
import br.com.fiap.healthtrack.domain.dto.response.ExameResponse;
import br.com.fiap.healthtrack.domain.enums.TipoExame;
import br.com.fiap.healthtrack.security.JwtService;
import br.com.fiap.healthtrack.security.UserDetailsServiceImpl;
import br.com.fiap.healthtrack.service.ExameService;
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

@WebMvcTest(ExameController.class)
@Import(TestSecurityConfig.class)
class ExameControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ExameService exameService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private static final ExameResponse EXAME = new ExameResponse(
            1L, 1L, "Maria Silva", TipoExame.SANGUE,
            "Hemograma completo", LocalDate.of(2026, 5, 20), "Normal", "LabTeste");

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("GET /exames/{pacienteId}/historico — retorna histórico paginado")
    void historico_retorna200() throws Exception {
        when(exameService.historico(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(EXAME)));

        mockMvc.perform(get("/exames/1/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipoExame").value("SANGUE"))
                .andExpect(jsonPath("$.content[0].laboratorio").value("LabTeste"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /exames — dados válidos retorna 201")
    void registrar_dadosValidos_retorna201() throws Exception {
        when(exameService.registrar(any())).thenReturn(EXAME);

        mockMvc.perform(post("/exames").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":1,"tipoExame":"SANGUE",
                                 "descricao":"Hemograma completo","dataRealizacao":"2026-05-20",
                                 "resultado":"Normal","laboratorio":"LabTeste"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomePaciente").value("Maria Silva"));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /exames — tipoExame inválido retorna 400 com valores aceitos")
    void registrar_tipoExameInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/exames").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":1,"tipoExame":"INVALIDO",
                                 "descricao":"Teste","dataRealizacao":"2026-05-20"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("SANGUE, IMAGEM, URINA")));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("POST /exames — campos obrigatórios ausentes retorna 400")
    void registrar_camposAusentes_retorna400() throws Exception {
        mockMvc.perform(post("/exames").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}

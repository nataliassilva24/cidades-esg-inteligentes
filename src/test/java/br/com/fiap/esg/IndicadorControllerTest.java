package br.com.fiap.esg;

import br.com.fiap.esg.model.Cidade;
import br.com.fiap.esg.repository.CidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndicadorControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CidadeRepository cidadeRepository;

    private Long cidadeId;

    @BeforeEach
    void prepararCidade() {
        cidadeRepository.deleteAll();
        cidadeId = cidadeRepository.save(new Cidade("Curitiba", "PR", 1_773_000L)).getId();
    }

    private String registrarIndicador(String pilar, String nome, double valor) throws Exception {
        return mvc.perform(post("/cidades/{id}/indicadores", cidadeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pilar": "%s", "nome": "%s", "valor": %s, "unidade": "%%", "dataReferencia": "2025-12-31"}
                                """.formatted(pilar, nome, valor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cidadeId").value(cidadeId))
                .andExpect(jsonPath("$.pilar").value(pilar))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    void deveRegistrarEListarIndicadoresPorPilar() throws Exception {
        registrarIndicador("AMBIENTAL", "Coleta seletiva", 42.5);
        registrarIndicador("AMBIENTAL", "Energia renovável na rede pública", 61.0);
        registrarIndicador("SOCIAL", "Cobertura de saneamento", 87.0);

        mvc.perform(get("/cidades/{id}/indicadores", cidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mvc.perform(get("/cidades/{id}/indicadores", cidadeId).param("pilar", "AMBIENTAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveGerarResumoEsgDaCidade() throws Exception {
        registrarIndicador("AMBIENTAL", "Coleta seletiva", 42.5);
        registrarIndicador("GOVERNANCA", "Transparência orçamentária", 9.1);

        mvc.perform(get("/cidades/{id}/resumo", cidadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIndicadores").value(2))
                .andExpect(jsonPath("$.indicadoresPorPilar.AMBIENTAL").value(1))
                .andExpect(jsonPath("$.indicadoresPorPilar.SOCIAL").value(0))
                .andExpect(jsonPath("$.indicadoresPorPilar.GOVERNANCA").value(1));
    }

    @Test
    void deveRejeitarIndicadorComDataFutura() throws Exception {
        String amanha = LocalDate.now().plusDays(1).toString();

        mvc.perform(post("/cidades/{id}/indicadores", cidadeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pilar": "SOCIAL", "nome": "IDH", "valor": 0.8, "dataReferencia": "%s"}
                                """.formatted(amanha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.dataReferencia").exists());
    }

    @Test
    void deveRemoverIndicador() throws Exception {
        String location = registrarIndicador("SOCIAL", "Cobertura de saneamento", 87.0);

        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoRegistrarIndicadorEmCidadeInexistente() throws Exception {
        mvc.perform(post("/cidades/{id}/indicadores", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pilar": "SOCIAL", "nome": "IDH", "valor": 0.8, "dataReferencia": "2025-01-01"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void endpointRaizDeveInformarAmbiente() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("test"));
    }
}

package br.com.fiap.esg;

import br.com.fiap.esg.repository.CidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CidadeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CidadeRepository cidadeRepository;

    @BeforeEach
    void limparBase() {
        cidadeRepository.deleteAll();
    }

    private String criarCidade(String nome, String uf) throws Exception {
        return mvc.perform(post("/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "%s", "uf": "%s", "populacao": 1000000}
                                """.formatted(nome, uf)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    void deveCriarEBuscarCidade() throws Exception {
        String location = criarCidade("Curitiba", "pr");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Curitiba"))
                .andExpect(jsonPath("$.uf").value("PR"));
    }

    @Test
    void deveFiltrarCidadesPorUf() throws Exception {
        criarCidade("São Paulo", "SP");
        criarCidade("Campinas", "SP");
        criarCidade("Recife", "PE");

        mvc.perform(get("/cidades").param("uf", "sp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveAtualizarERemoverCidade() throws Exception {
        String location = criarCidade("Florianopolis", "SC");

        mvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Florianópolis", "uf": "SC", "populacao": 537000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Florianópolis"))
                .andExpect(jsonPath("$.populacao").value(537000));

        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarCidadeInvalida() throws Exception {
        mvc.perform(post("/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "uf": "SAO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.uf").exists());
    }

    @Test
    void deveRetornar404ParaCidadeInexistente() throws Exception {
        mvc.perform(get("/cidades/999999")).andExpect(status().isNotFound());
    }
}

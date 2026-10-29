package br.com.fiap.healthtrack.bdd.steps;

import br.com.fiap.healthtrack.bdd.support.ApiContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * Passos genéricos de API usados por todas as funcionalidades:
 * autenticação, envio de requisições, validação de status code, corpo JSON e contrato (JSON Schema).
 */
public class ApiSteps {

    private final ApiContext ctx;

    public ApiSteps(ApiContext ctx) {
        this.ctx = ctx;
    }

    // ---------- contexto ----------

    @Dado("que estou autenticado como {string}")
    public void queEstouAutenticadoComo(String perfil) {
        ctx.autenticarComo(perfil);
    }

    @Dado("que não estou autenticado")
    public void queNaoEstouAutenticado() {
        ctx.removerAutenticacao();
    }

    @Dado("que estou usando um token inválido")
    public void queEstouUsandoUmTokenInvalido() {
        ctx.usarToken("token.invalido.qualquer");
    }

    @Dado("que existe um paciente cadastrado")
    public void queExisteUmPacienteCadastrado() {
        ctx.garantirPacienteCadastrado();
    }

    @Dado("que existe um médico cadastrado")
    public void queExisteUmMedicoCadastrado() {
        ctx.garantirMedicoExistente();
    }

    @Dado("que gerei um CPF válido")
    public void queGereiUmCpfValido() {
        ctx.novoCpf();
    }

    // ---------- ação ----------

    @Quando("envio uma requisição {word} para {string}")
    public void envioUmaRequisicao(String metodo, String caminho) {
        ctx.enviar(metodo, caminho, null);
    }

    @Quando("envio uma requisição {word} para {string} com o corpo:")
    public void envioUmaRequisicaoComCorpo(String metodo, String caminho, String corpo) {
        ctx.enviar(metodo, caminho, corpo);
    }

    // ---------- validações ----------

    @Então("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int status) {
        ctx.ultimaResposta().then().statusCode(status);
    }

    @E("a resposta deve respeitar o contrato {string}")
    public void aRespostaDeveRespeitarOContrato(String schema) {
        ctx.ultimaResposta().then().assertThat().body(matchesJsonSchemaInClasspath("schemas/" + schema));
    }

    @E("o campo {string} deve ser {string}")
    public void oCampoDeveSer(String campo, String valor) {
        ctx.ultimaResposta().then().body(campo, hasToString(ctx.resolver(valor)));
    }

    @E("o campo {string} deve ser o número {int}")
    public void oCampoDeveSerONumero(String campo, int valor) {
        ctx.ultimaResposta().then().body(campo, equalTo(valor));
    }

    @E("o campo {string} deve conter {string}")
    public void oCampoDeveConter(String campo, String trecho) {
        ctx.ultimaResposta().then().body(campo, containsString(ctx.resolver(trecho)));
    }

    @E("o campo {string} deve estar preenchido")
    public void oCampoDeveEstarPreenchido(String campo) {
        ctx.ultimaResposta().then().body(campo, allOf(notNullValue(), not(hasToString(""))));
    }

    @E("a lista {string} não deve estar vazia")
    public void aListaNaoDeveEstarVazia(String campo) {
        ctx.ultimaResposta().then().body(campo, not(empty()));
    }

    @E("todos os itens de {string} devem ter o campo {string} igual a {string}")
    public void todosOsItensDevemTerOCampoIgualA(String lista, String campo, String valor) {
        ctx.ultimaResposta().then().body(lista + "." + campo, everyItem(hasToString(ctx.resolver(valor))));
    }

    @E("a lista de erros deve apontar o campo {string}")
    public void aListaDeErrosDeveApontarOCampo(String campo) {
        ctx.ultimaResposta().then().body("errors.campo", hasItem(campo));
    }

    @E("o corpo da resposta deve estar vazio")
    public void oCorpoDaRespostaDeveEstarVazio() {
        ctx.ultimaResposta().then().body(emptyOrNullString());
    }

    @E("o indicador {string} deve ter aumentado em relação a {string}")
    public void oIndicadorDeveTerAumentado(String campo, String variavelAnterior) {
        long anterior = Long.parseLong(ctx.variavel(variavelAnterior));
        long atual = ctx.ultimaResposta().jsonPath().getLong(campo);
        org.junit.jupiter.api.Assertions.assertTrue(atual > anterior,
                "Esperado que '" + campo + "' aumentasse de " + anterior + ", mas veio " + atual);
    }

    @E("guardo o campo {string} da resposta como {string}")
    public void guardoOCampoDaRespostaComo(String campo, String variavel) {
        Response resposta = ctx.ultimaResposta();
        ctx.guardar(variavel, resposta.jsonPath().getString(campo));
    }
}

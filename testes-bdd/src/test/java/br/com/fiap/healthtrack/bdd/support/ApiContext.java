package br.com.fiap.healthtrack.bdd.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estado compartilhado de um cenário: URL base, token atual, última resposta e variáveis
 * (ex.: {pacienteId}, {cpfValido}) usadas para montar URLs e corpos das requisições.
 * O Cucumber cria uma instância nova por cenário (PicoContainer), garantindo isolamento.
 */
public class ApiContext {

    private static final Pattern VARIAVEL = Pattern.compile("\\{(\\w+)}");

    /** Tokens por perfil são reaproveitados entre cenários para não registrar usuários à toa. */
    private static final Map<String, String> TOKENS_POR_PERFIL = new ConcurrentHashMap<>();

    private final Map<String, String> variaveis = new HashMap<>();
    private String token;
    private Response ultimaResposta;

    public ApiContext() {
        RestAssured.baseURI = System.getProperty("baseUrl",
                Optional.ofNullable(System.getenv("BASE_URL")).orElse("http://localhost:8080"));
        variaveis.put("execucao", UUID.randomUUID().toString().substring(0, 8));
    }

    // ---------- requisições ----------

    public RequestSpecification requisicao() {
        RequestSpecification spec = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON);
        if (token != null) {
            spec.header("Authorization", "Bearer " + token);
        }
        return spec;
    }

    public Response enviar(String metodo, String caminho, String corpo) {
        RequestSpecification spec = requisicao();
        if (corpo != null) {
            spec.body(resolver(corpo));
        }
        ultimaResposta = spec.request(metodo, resolver(caminho)).then().extract().response();
        return ultimaResposta;
    }

    public Response ultimaResposta() {
        return ultimaResposta;
    }

    // ---------- autenticação ----------

    public void autenticarComo(String perfil) {
        token = TOKENS_POR_PERFIL.computeIfAbsent(perfil, this::registrarUsuario);
    }

    public void removerAutenticacao() {
        token = null;
    }

    public void usarToken(String novoToken) {
        token = novoToken;
    }

    private String registrarUsuario(String perfil) {
        String email = "bdd." + perfil.toLowerCase() + "." + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        Map<String, Object> corpo = Map.of(
                "nome", "Usuário BDD " + perfil,
                "email", email,
                "senha", "senha123",
                "tipoUsuario", perfil);
        Response resposta = RestAssured.given().contentType(ContentType.JSON).body(corpo)
                .post("/auth/register").then().statusCode(201).extract().response();
        return resposta.jsonPath().getString("token");
    }

    // ---------- massa de dados ----------

    /** Cadastra um paciente novo (como ADMIN) e guarda o id em {pacienteId}. */
    public void garantirPacienteCadastrado() {
        String tokenAnterior = token;
        autenticarComo("ADMIN");
        String corpo = """
                {"nome": "Paciente BDD {execucao}", "cpf": "%s", "dataNascimento": "1990-05-20",
                 "telefone": "(11) 91234-5678", "email": "paciente.{execucao}@teste.com",
                 "endereco": "Av. Paulista, 1000, São Paulo - SP"}
                """.formatted(novoCpf());
        Response resposta = enviar("POST", "/pacientes", corpo);
        resposta.then().statusCode(201);
        variaveis.put("pacienteId", resposta.jsonPath().getString("id"));
        token = tokenAnterior;
    }

    /**
     * Descobre o id de um médico já existente a partir dos dados iniciais (migração V5):
     * o prontuário da paciente semente é sempre registrado pelo Dr. João Silva.
     */
    public void garantirMedicoExistente() {
        String tokenAnterior = token;
        autenticarComo("ADMIN");
        List<Map<String, Object>> pacientes = enviar("GET", "/pacientes?size=1000", null)
                .jsonPath().getList("content");
        Object pacienteSemente = pacientes.stream()
                .filter(p -> "123.456.789-09".equals(p.get("cpf")))
                .findFirst().orElseThrow(() -> new IllegalStateException("Paciente semente não encontrado"))
                .get("id");
        String medicoId = enviar("GET", "/prontuarios/" + pacienteSemente, null)
                .jsonPath().getString("content[0].medicoId");
        variaveis.put("medicoId", medicoId);
        token = tokenAnterior;
    }

    /** Gera um CPF válido (dígitos verificadores corretos) e o guarda em {cpfValido}. */
    public String novoCpf() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[9] = digitoVerificador(d, 9);
        d[10] = digitoVerificador(d, 10);
        String cpf = String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9], d[10]);
        variaveis.put("cpfValido", cpf);
        return cpf;
    }

    private static int digitoVerificador(int[] d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += d[i] * (tamanho + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    // ---------- variáveis ----------

    public void guardar(String nome, String valor) {
        variaveis.put(nome, valor);
    }

    public String variavel(String nome) {
        return variaveis.get(nome);
    }

    /** Substitui {variavel} pelos valores guardados no cenário. */
    public String resolver(String texto) {
        Matcher m = VARIAVEL.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String valor = variaveis.get(m.group(1));
            if (valor == null) {
                throw new IllegalStateException("Variável não definida no cenário: {" + m.group(1) + "}");
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(valor));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

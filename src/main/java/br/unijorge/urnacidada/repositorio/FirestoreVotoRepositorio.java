package br.unijorge.urnacidada.repositorio;

import br.unijorge.urnacidada.infra.FirestoreMapper;
import br.unijorge.urnacidada.infra.Json;
import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Persistência no Firebase Cloud Firestore usando a API REST oficial
 * (https://firebase.google.com/docs/firestore/use-rest-api).
 *
 * <p>Estrutura no banco:
 * <pre>
 * eleicoes/{eleicaoId}/votos/{idGeradoPeloFirestore}
 *     cargo:        "VEREADOR" | "PREFEITO"
 *     tipo:         "NOMINAL" | "LEGENDA" | "BRANCO" | "NULO"
 *     numero:       "21123"
 *     partido:      "PIPA"
 *     registradoEm: timestamp
 *     urnaId:       "urna-01"
 * </pre>
 * Usamos só a biblioteca padrão do Java (java.net.http), sem dependências externas.</p>
 */
public class FirestoreVotoRepositorio implements VotoRepositorio {

    private static final String BASE = "https://firestore.googleapis.com/v1/";

    private final String projectId;
    private final String apiKey;
    private final String eleicaoId;
    private final HttpClient http;

    public FirestoreVotoRepositorio(String projectId, String apiKey, String eleicaoId) {
        if (projectId == null || projectId.isBlank()) throw new IllegalArgumentException("projectId do Firebase não configurado");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("apiKey do Firebase não configurada");
        this.projectId = projectId.trim();
        this.apiKey = apiKey.trim();
        this.eleicaoId = eleicaoId;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    String urlColecao() {
        return BASE + "projects/" + projectId + "/databases/(default)/documents/eleicoes/"
                + URLEncoder.encode(eleicaoId, StandardCharsets.UTF_8) + "/votos";
    }

    @Override
    public void salvar(RegistroVoto registro) {
        String corpo = Json.escrever(FirestoreMapper.paraDocumento(registro));
        HttpRequest req = HttpRequest.newBuilder(URI.create(urlColecao() + "?key=" + apiKey))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8))
                .build();
        enviar(req);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RegistroVoto> listarTodos() {
        List<RegistroVoto> lista = new ArrayList<>();
        String pageToken = null;
        do {
            String url = urlColecao() + "?pageSize=300&key=" + apiKey
                    + (pageToken != null ? "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8) : "");
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).GET().build();
            Map<String, Object> resposta = (Map<String, Object>) Json.ler(enviar(req));
            Object docs = resposta.get("documents");
            if (docs instanceof List<?> l) {
                for (Object d : l) lista.add(FirestoreMapper.deDocumento((Map<String, Object>) d));
            }
            pageToken = (String) resposta.get("nextPageToken");
        } while (pageToken != null && !pageToken.isEmpty());
        return lista;
    }

    private String enviar(HttpRequest req) {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new RepositorioException("Firestore respondeu HTTP " + resp.statusCode() + ": " + resumir(resp.body()));
            }
            return resp.body();
        } catch (IOException e) {
            throw new RepositorioException("Sem conexão com o Firestore: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositorioException("Operação interrompida", e);
        }
    }

    private static String resumir(String s) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ");
        return s.length() > 200 ? s.substring(0, 200) + "…" : s;
    }

    @Override
    public String getDescricao() { return "Firebase Cloud Firestore (" + projectId + ")"; }
}

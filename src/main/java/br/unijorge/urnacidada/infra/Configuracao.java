package br.unijorge.urnacidada.infra;

import br.unijorge.urnacidada.repositorio.ArquivoVotoRepositorio;
import br.unijorge.urnacidada.repositorio.FirestoreVotoRepositorio;
import br.unijorge.urnacidada.repositorio.MemoriaVotoRepositorio;
import br.unijorge.urnacidada.repositorio.RepositorioComContingencia;
import br.unijorge.urnacidada.repositorio.VotoRepositorio;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lê o arquivo {@code config.properties} (ao lado do programa) e monta o
 * repositório de votos adequado. Variáveis de ambiente têm prioridade
 * (ex.: FIREBASE_API_KEY), para não precisar deixar a chave no arquivo.
 */
public class Configuracao {

    private final Properties props = new Properties();

    public Configuracao(Path arquivo) {
        if (arquivo != null && Files.exists(arquivo)) {
            try (Reader r = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                props.load(r);
            } catch (IOException e) {
                System.err.println("Aviso: não foi possível ler " + arquivo + ": " + e.getMessage());
            }
        }
    }

    public String get(String chave, String padrao) {
        String env = System.getenv(chave.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env.trim();
        String v = props.getProperty(chave);
        return v == null || v.isBlank() ? padrao : v.trim();
    }

    /** "memoria", "arquivo" ou "firestore". */
    public String getModo() { return get("persistencia.modo", "arquivo").toLowerCase(); }
    public String getEleicaoId() { return get("eleicao.id", "oficina-2026"); }
    public String getUrnaId() { return get("urna.id", "urna-01"); }
    public String getPinMesario() { return get("mesario.pin", "1234"); }

    public Path getPastaDados() {
        return Path.of(get("dados.pasta", System.getProperty("user.home") + "/.urna-cidada"));
    }

    public VotoRepositorio criarRepositorio() {
        ArquivoVotoRepositorio arquivo = new ArquivoVotoRepositorio(
                getPastaDados().resolve("votos-" + getEleicaoId() + ".jsonl"));
        return switch (getModo()) {
            case "memoria" -> new MemoriaVotoRepositorio();
            case "firestore" -> new RepositorioComContingencia(
                    new FirestoreVotoRepositorio(get("firebase.projectId", null), get("firebase.apiKey", null), getEleicaoId()),
                    new ArquivoVotoRepositorio(getPastaDados().resolve("contingencia-" + getEleicaoId() + ".jsonl")));
            default -> arquivo;
        };
    }
}

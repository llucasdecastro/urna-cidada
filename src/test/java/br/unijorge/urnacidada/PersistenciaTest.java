package br.unijorge.urnacidada;

import br.unijorge.urnacidada.infra.FirestoreMapper;
import br.unijorge.urnacidada.infra.Json;
import br.unijorge.urnacidada.modelo.RegistroVoto;
import br.unijorge.urnacidada.modelo.TipoVoto;
import br.unijorge.urnacidada.repositorio.ArquivoVotoRepositorio;
import br.unijorge.urnacidada.repositorio.MemoriaVotoRepositorio;
import br.unijorge.urnacidada.repositorio.RepositorioComContingencia;
import br.unijorge.urnacidada.repositorio.RepositorioException;
import br.unijorge.urnacidada.repositorio.VotoRepositorio;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PersistenciaTest {

    private final RegistroVoto exemplo = new RegistroVoto("VEREADOR", TipoVoto.NOMINAL, "21123", "PIPA",
            Instant.parse("2026-11-20T14:30:00Z"), "urna-01");

    @Test
    public void jsonIdaEVolta() {
        String json = Json.escrever(Map.of("texto", "Olá \"mundo\"\n", "lista", List.of(1L, true)));
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) Json.ler(json);
        assertEquals("Olá \"mundo\"\n", m.get("texto"));
        assertEquals(List.of(1L, true), m.get("lista"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void documentoFirestoreIdaEVolta() {
        String json = Json.escrever(FirestoreMapper.paraDocumento(exemplo));
        assertTrue(json.contains("\"stringValue\":\"VEREADOR\""));
        assertTrue(json.contains("\"timestampValue\":\"2026-11-20T14:30:00Z\""));
        // simula a resposta do Firestore, que inclui "name" e datas
        String resposta = "{\"name\":\"projects/x/databases/(default)/documents/eleicoes/e/votos/" + exemplo.id() + "\","
                + json.substring(1, json.length() - 1) + ",\"createTime\":\"2026-11-20T14:30:01Z\"}";
        RegistroVoto volta = FirestoreMapper.deDocumento((Map<String, Object>) Json.ler(resposta));
        assertEquals(exemplo, volta);
    }

    @Test
    public void legendaSemPartidoNuloVaiComoNullValue() {
        RegistroVoto branco = new RegistroVoto("PREFEITO", TipoVoto.BRANCO, "", null, Instant.now(), "u");
        assertTrue(Json.escrever(FirestoreMapper.paraDocumento(branco)).contains("nullValue"));
    }

    @Test
    public void arquivoGravaELe() throws Exception {
        Path tmp = Files.createTempDirectory("urna").resolve("votos.jsonl");
        ArquivoVotoRepositorio repo = new ArquivoVotoRepositorio(tmp);
        repo.salvar(exemplo);
        repo.salvar(exemplo);
        assertEquals(List.of(exemplo, exemplo), new ArquivoVotoRepositorio(tmp).listarTodos());
    }

    @Test
    public void contingenciaGuardaLocalQuandoNuvemFalhaESincronizaDepois() throws Exception {
        NuvemFalsa nuvem = new NuvemFalsa();
        ArquivoVotoRepositorio local = new ArquivoVotoRepositorio(Files.createTempDirectory("urna").resolve("c.jsonl"));
        RepositorioComContingencia repo = new RepositorioComContingencia(nuvem, local);

        nuvem.online = false;
        repo.salvar(exemplo);
        assertEquals(1, repo.getQuantidadePendentes());
        assertNotNull(repo.getUltimoErro());
        assertEquals(1, repo.listarTodos().size()); // apuração ainda enxerga o voto local

        nuvem.online = true;
        assertEquals(1, repo.sincronizarPendentes());
        assertEquals(0, repo.getQuantidadePendentes());
        assertEquals(1, nuvem.listarTodos().size());
        assertEquals(1, repo.listarTodos().size()); // sem duplicar
    }

    @Test
    public void respostaPerdidaNaoDuplicaVoto() throws Exception {
        NuvemFalsa nuvem = new NuvemFalsa();
        ArquivoVotoRepositorio local = new ArquivoVotoRepositorio(Files.createTempDirectory("urna").resolve("c.jsonl"));
        RepositorioComContingencia repo = new RepositorioComContingencia(nuvem, local);

        nuvem.perderResposta = true;
        repo.salvar(exemplo);
        assertEquals(1, repo.getQuantidadePendentes());
        assertEquals(1, repo.listarTodos().size());

        assertEquals(1, repo.sincronizarPendentes());
        assertEquals(0, repo.getQuantidadePendentes());
        assertEquals(1, nuvem.listarTodos().size());
    }

    /** Repositório que simula a nuvem caindo. */
    private static class NuvemFalsa implements VotoRepositorio {
        boolean online = true;
        boolean perderResposta;
        private final MemoriaVotoRepositorio dados = new MemoriaVotoRepositorio();

        @Override public void salvar(RegistroVoto r) {
            if (!online) throw new RepositorioException("sem internet");
            if (dados.listarTodos().stream().noneMatch(v -> v.id().equals(r.id()))) dados.salvar(r);
            if (perderResposta) {
                perderResposta = false;
                throw new RepositorioException("resposta perdida");
            }
        }
        @Override public List<RegistroVoto> listarTodos() {
            if (!online) throw new RepositorioException("sem internet");
            return dados.listarTodos();
        }
        @Override public String getDescricao() { return "nuvem falsa"; }
    }
}

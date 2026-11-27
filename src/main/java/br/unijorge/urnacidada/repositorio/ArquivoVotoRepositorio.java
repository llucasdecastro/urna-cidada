package br.unijorge.urnacidada.repositorio;

import br.unijorge.urnacidada.infra.FirestoreMapper;
import br.unijorge.urnacidada.infra.Json;
import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Grava cada voto como uma linha JSON em um arquivo local.
 * Funciona sem internet e é usado como contingência quando o Firestore falha.
 */
public class ArquivoVotoRepositorio implements VotoRepositorio {

    private final Path arquivo;

    public ArquivoVotoRepositorio(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public synchronized void salvar(RegistroVoto registro) {
        try {
            if (arquivo.getParent() != null) Files.createDirectories(arquivo.getParent());
            String linha = Json.escrever(FirestoreMapper.paraMapaSimples(registro)) + System.lineSeparator();
            Files.writeString(arquivo, linha, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RepositorioException("Não foi possível gravar no arquivo " + arquivo, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<RegistroVoto> listarTodos() {
        if (!Files.exists(arquivo)) return List.of();
        try {
            List<RegistroVoto> lista = new ArrayList<>();
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                if (linha.isBlank()) continue;
                lista.add(FirestoreMapper.deMapaSimples((Map<String, Object>) Json.ler(linha)));
            }
            return lista;
        } catch (IOException | RuntimeException e) {
            throw new RepositorioException("Não foi possível ler o arquivo " + arquivo, e);
        }
    }

    /** Remove o arquivo (usado após sincronizar a contingência com a nuvem). */
    public synchronized void esvaziar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new RepositorioException("Não foi possível limpar " + arquivo, e);
        }
    }

    public Path getArquivo() { return arquivo; }

    @Override
    public String getDescricao() { return "Arquivo local (" + arquivo.getFileName() + ")"; }
}

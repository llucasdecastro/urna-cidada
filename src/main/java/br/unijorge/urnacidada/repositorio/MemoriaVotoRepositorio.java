package br.unijorge.urnacidada.repositorio;

import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.util.ArrayList;
import java.util.List;

/** Guarda os votos só na memória. Útil para testes e demonstrações rápidas. */
public class MemoriaVotoRepositorio implements VotoRepositorio {

    private final List<RegistroVoto> registros = new ArrayList<>();

    @Override
    public synchronized void salvar(RegistroVoto registro) { registros.add(registro); }

    @Override
    public synchronized List<RegistroVoto> listarTodos() { return List.copyOf(registros); }

    @Override
    public String getDescricao() { return "Memória (os votos somem ao fechar)"; }
}

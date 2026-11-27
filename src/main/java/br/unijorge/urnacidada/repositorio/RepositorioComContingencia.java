package br.unijorge.urnacidada.repositorio;

import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator: tenta gravar no repositório principal (nuvem) e, se falhar,
 * grava no arquivo de contingência para não perder o voto. Depois, os votos
 * pendentes podem ser enviados com {@link #sincronizarPendentes()}.
 *
 * <p>Na oficina a internet da escola pode cair; assim a votação nunca para.</p>
 */
public class RepositorioComContingencia implements VotoRepositorio {

    private final VotoRepositorio principal;
    private final ArquivoVotoRepositorio contingencia;
    private String ultimoErro;

    public RepositorioComContingencia(VotoRepositorio principal, ArquivoVotoRepositorio contingencia) {
        this.principal = principal;
        this.contingencia = contingencia;
    }

    @Override
    public void salvar(RegistroVoto registro) {
        try {
            principal.salvar(registro);
            ultimoErro = null;
        } catch (RepositorioException e) {
            ultimoErro = e.getMessage();
            contingencia.salvar(registro);
        }
    }

    @Override
    public List<RegistroVoto> listarTodos() {
        List<RegistroVoto> todos = new ArrayList<>();
        try {
            todos.addAll(principal.listarTodos());
            ultimoErro = null;
        } catch (RepositorioException e) {
            ultimoErro = e.getMessage();
        }
        todos.addAll(contingencia.listarTodos());
        return todos;
    }

    /** Envia para a nuvem os votos guardados localmente. Retorna quantos foram enviados. */
    public synchronized int sincronizarPendentes() {
        List<RegistroVoto> pendentes = contingencia.listarTodos();
        int enviados = 0;
        List<RegistroVoto> restantes = new ArrayList<>();
        for (RegistroVoto r : pendentes) {
            try {
                principal.salvar(r);
                enviados++;
            } catch (RepositorioException e) {
                ultimoErro = e.getMessage();
                restantes.add(r);
            }
        }
        contingencia.esvaziar();
        restantes.forEach(contingencia::salvar);
        return enviados;
    }

    public int getQuantidadePendentes() { return contingencia.listarTodos().size(); }

    /** Último erro de comunicação com a nuvem, ou null se a última operação deu certo. */
    public String getUltimoErro() { return ultimoErro; }

    @Override
    public String getDescricao() { return principal.getDescricao() + " + contingência local"; }
}

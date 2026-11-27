package br.unijorge.urnacidada.modelo;

import java.util.Objects;

/**
 * Candidato FICTÍCIO. Classe abstrata: todo candidato concorre a um cargo
 * e pertence a um partido, mas cada tipo exibe informações diferentes na tela.
 */
public abstract class Candidato {

    private final String numero;
    private final String nome;
    private final Partido partido;
    private final String bandeira;

    protected Candidato(String numero, String nome, Partido partido, String bandeira) {
        this.numero = Objects.requireNonNull(numero);
        this.nome = Objects.requireNonNull(nome);
        this.partido = Objects.requireNonNull(partido);
        this.bandeira = bandeira;
        if (!numero.startsWith(String.valueOf(partido.getNumero()))) {
            throw new IllegalArgumentException(
                    "O número " + numero + " deve começar com o número do partido " + partido.getNumero());
        }
    }

    public String getNumero() { return numero; }
    public String getNome() { return nome; }
    public Partido getPartido() { return partido; }
    /** Proposta principal (fictícia) usada na oficina. */
    public String getBandeira() { return bandeira; }

    public abstract Cargo getCargo();

    /** Linhas extras exibidas na tela da urna (ex.: vice). Polimorfismo. */
    public String getInformacaoComplementar() { return null; }

    /** Iniciais usadas no "retrato" desenhado na tela (não usamos fotos reais). */
    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) ini += partes[partes.length - 1].substring(0, 1);
        return ini.toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Candidato c && c.numero.equals(numero) && c.getCargo().getChave().equals(getCargo().getChave());
    }

    @Override
    public int hashCode() { return Objects.hash(numero, getCargo().getChave()); }

    @Override
    public String toString() { return numero + " - " + nome + " (" + partido.getSigla() + ")"; }
}

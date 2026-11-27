package br.unijorge.urnacidada.modelo;

import java.awt.Color;
import java.util.Objects;

/**
 * Partido político FICTÍCIO usado na simulação.
 * Nenhum partido deste sistema tem vínculo com legendas reais.
 */
public class Partido {

    private final int numero;
    private final String sigla;
    private final String nome;
    private final String tema;
    private final Color cor;

    public Partido(int numero, String sigla, String nome, String tema, Color cor) {
        if (numero < 10 || numero > 99) {
            throw new IllegalArgumentException("O número do partido deve ter 2 dígitos: " + numero);
        }
        this.numero = numero;
        this.sigla = Objects.requireNonNull(sigla);
        this.nome = Objects.requireNonNull(nome);
        this.tema = tema;
        this.cor = cor == null ? Color.GRAY : cor;
    }

    public int getNumero() { return numero; }
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public String getTema() { return tema; }
    public Color getCor() { return cor; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Partido p && p.numero == numero;
    }

    @Override
    public int hashCode() { return Integer.hashCode(numero); }

    @Override
    public String toString() { return sigla + " (" + numero + ")"; }
}

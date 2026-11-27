package br.unijorge.urnacidada.modelo;

/** Número digitado que não corresponde a candidato nem a partido. */
public class VotoNulo extends Voto {

    private final String numeroDigitado;

    public VotoNulo(Cargo cargo, String numeroDigitado) {
        super(cargo);
        this.numeroDigitado = numeroDigitado == null ? "" : numeroDigitado;
    }

    @Override
    public TipoVoto getTipo() { return TipoVoto.NULO; }

    @Override
    public String getNumero() { return numeroDigitado; }
}

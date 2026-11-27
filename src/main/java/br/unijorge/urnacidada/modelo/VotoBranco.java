package br.unijorge.urnacidada.modelo;

public class VotoBranco extends Voto {

    public VotoBranco(Cargo cargo) { super(cargo); }

    @Override
    public TipoVoto getTipo() { return TipoVoto.BRANCO; }

    @Override
    public String getNumero() { return ""; }
}

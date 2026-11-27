package br.unijorge.urnacidada.modelo;

/** Voto em um candidato específico. */
public class VotoNominal extends Voto {

    private final Candidato candidato;

    public VotoNominal(Candidato candidato) {
        super(candidato.getCargo());
        this.candidato = candidato;
    }

    public Candidato getCandidato() { return candidato; }

    @Override
    public TipoVoto getTipo() { return TipoVoto.NOMINAL; }

    @Override
    public String getNumero() { return candidato.getNumero(); }

    @Override
    public String getSiglaPartido() { return candidato.getPartido().getSigla(); }
}

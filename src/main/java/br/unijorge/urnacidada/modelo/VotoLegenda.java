package br.unijorge.urnacidada.modelo;

/** Voto apenas no partido (só existe em cargos proporcionais). */
public class VotoLegenda extends Voto {

    private final Partido partido;

    public VotoLegenda(Cargo cargo, Partido partido) {
        super(cargo);
        if (!cargo.aceitaVotoDeLegenda()) {
            throw new IllegalArgumentException(cargo.getNome() + " não aceita voto de legenda");
        }
        this.partido = partido;
    }

    public Partido getPartido() { return partido; }

    @Override
    public TipoVoto getTipo() { return TipoVoto.LEGENDA; }

    @Override
    public String getNumero() { return String.valueOf(partido.getNumero()); }

    @Override
    public String getSiglaPartido() { return partido.getSigla(); }
}

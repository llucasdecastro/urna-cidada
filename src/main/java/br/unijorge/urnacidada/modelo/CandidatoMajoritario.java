package br.unijorge.urnacidada.modelo;

/** Candidato a cargo majoritário: concorre numa chapa com um vice. */
public class CandidatoMajoritario extends Candidato {

    private final CargoMajoritario cargo;
    private final String vice;

    public CandidatoMajoritario(String nome, String vice, Partido partido,
                                CargoMajoritario cargo, String bandeira) {
        super(String.valueOf(partido.getNumero()), nome, partido, bandeira);
        this.cargo = cargo;
        this.vice = vice;
    }

    public String getVice() { return vice; }

    @Override
    public Cargo getCargo() { return cargo; }

    @Override
    public String getInformacaoComplementar() {
        return vice == null ? null : "Vice-" + cargo.getNome() + ": " + vice;
    }
}

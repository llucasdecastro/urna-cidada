package br.unijorge.urnacidada.modelo;

public class CandidatoProporcional extends Candidato {

    private final CargoProporcional cargo;

    public CandidatoProporcional(String numero, String nome, Partido partido,
                                 CargoProporcional cargo, String bandeira) {
        super(numero, nome, partido, bandeira);
        if (numero.length() != cargo.getQuantidadeDigitos()) {
            throw new IllegalArgumentException("Número de " + cargo.getNome() + " deve ter "
                    + cargo.getQuantidadeDigitos() + " dígitos: " + numero);
        }
        this.cargo = cargo;
    }

    @Override
    public Cargo getCargo() { return cargo; }
}

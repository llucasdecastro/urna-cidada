package br.unijorge.urnacidada.modelo;

/**
 * Cargo proporcional (ex.: vereador, deputado).
 * O número do candidato começa com o número do partido e o eleitor pode votar
 * só no partido digitando os 2 primeiros dígitos (voto de legenda).
 */
public class CargoProporcional extends Cargo {

    private final int vagas;

    public CargoProporcional(String nome, int quantidadeDigitos, int vagas) {
        super(nome, quantidadeDigitos);
        if (quantidadeDigitos <= 2) {
            throw new IllegalArgumentException("Cargo proporcional precisa de mais de 2 dígitos");
        }
        this.vagas = vagas;
    }

    public int getVagas() { return vagas; }

    @Override
    public boolean aceitaVotoDeLegenda() { return true; }

    @Override
    public SistemaEleitoral getSistema() { return SistemaEleitoral.PROPORCIONAL; }

    @Override
    public String explicacaoDaRegra() {
        return "Sistema proporcional: as " + vagas + " vagas são divididas entre os partidos "
                + "conforme o total de votos de cada um (votos nos candidatos + votos de legenda). "
                + "Dentro de cada partido, ficam com as vagas os candidatos mais votados, desde que tenham "
                + "pelo menos 10% do quociente eleitoral.";
    }
}

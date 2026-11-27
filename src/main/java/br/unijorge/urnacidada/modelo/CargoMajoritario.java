package br.unijorge.urnacidada.modelo;

/**
 * Cargo majoritário (ex.: prefeito, presidente).
 * O número do candidato é o próprio número do partido (2 dígitos) e vence
 * quem tiver mais votos válidos. Não existe voto de legenda.
 */
public class CargoMajoritario extends Cargo {

    public CargoMajoritario(String nome) {
        super(nome, 2);
    }

    @Override
    public boolean aceitaVotoDeLegenda() { return false; }

    @Override
    public SistemaEleitoral getSistema() { return SistemaEleitoral.MAJORITARIO; }

    @Override
    public String explicacaoDaRegra() {
        return "Sistema majoritário: vence quem tiver mais votos válidos. Nas cidades com mais "
                + "de 200 mil eleitores, se ninguém passar de 50% dos votos válidos, há 2º turno. "
                + "Votos brancos e nulos não entram na conta dos votos válidos.";
    }
}

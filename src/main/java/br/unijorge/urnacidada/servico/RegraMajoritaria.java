package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Eleicao;

import java.util.List;

/** Vence quem tiver mais votos (cidade pequena, turno único). */
public class RegraMajoritaria implements RegraApuracao {

    @Override
    public void definirEleitos(ResultadoCargo resultado, Eleicao eleicao) {
        List<Candidato> ranking = resultado.ranking();
        if (ranking.isEmpty() || resultado.votosDe(ranking.get(0)) == 0) {
            resultado.definirEleitos(List.of());
            return;
        }
        int maior = resultado.votosDe(ranking.get(0));
        List<Candidato> empatados = ranking.stream().filter(c -> resultado.votosDe(c) == maior).toList();
        resultado.definirEmpate(empatados.size() > 1);
        // Em caso de empate a lei dá a vitória ao mais idoso; na simulação apenas sinalizamos.
        resultado.definirEleitos(empatados.size() == 1 ? empatados : List.of());
    }
}

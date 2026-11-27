package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.CargoProporcional;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.Partido;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Versão didática e simplificada do sistema proporcional brasileiro:
 * <ol>
 *   <li>Quociente eleitoral (QE) = votos válidos ÷ vagas (arredondado; fração &gt; 0,5 sobe).</li>
 *   <li>Quociente partidário = votos do partido ÷ QE (parte inteira) → vagas diretas.</li>
 *   <li>Vagas que sobram vão, uma a uma, ao partido com a maior média
 *       (votos do partido ÷ (vagas já obtidas + 1)).</li>
 *   <li>Dentro do partido, as vagas vão aos candidatos mais votados que tenham
 *       pelo menos 10% do QE (e no mínimo 1 voto). Sem candidato apto, a vaga
 *       passa para outro partido; se ninguém puder recebê-la, fica sem preencher.</li>
 * </ol>
 * Simplificações: não aplicamos a exigência de 20% do QE para as sobras nem
 * de 80% do QE para o partido, para manter a regra compreensível na oficina.
 */
public class RegraProporcional implements RegraApuracao {

    @Override
    public void definirEleitos(ResultadoCargo resultado, Eleicao eleicao) {
        CargoProporcional cargo = (CargoProporcional) resultado.getCargo();
        int vagas = cargo.getVagas();
        int validos = resultado.getVotosValidos();
        if (validos == 0) {
            resultado.definirQuocienteEleitoral(null);
            resultado.definirEleitos(List.of());
            resultado.definirVagas(Map.of());
            return;
        }

        int qe = (int) Math.round((double) validos / vagas);
        if (qe == 0) qe = 1;
        resultado.definirQuocienteEleitoral(qe);
        final int minimo = Math.max(1, (int) Math.ceil(qe * 0.10));

        Map<Partido, Integer> vagasPorPartido = new LinkedHashMap<>();
        Map<Partido, Integer> candidatosDisponiveis = new LinkedHashMap<>();
        for (Partido p : eleicao.getPartidos()) {
            int disponiveis = (int) eleicao.candidatosDo(cargo).stream()
                    .filter(c -> c.getPartido().equals(p) && apto(resultado, c, minimo)).count();
            candidatosDisponiveis.put(p, disponiveis);
            int qp = resultado.votosDoPartido(p) / qe;
            vagasPorPartido.put(p, Math.min(qp, disponiveis));
        }

        int distribuidas = vagasPorPartido.values().stream().mapToInt(Integer::intValue).sum();
        while (distribuidas < vagas) {
            Partido melhor = null;
            double melhorMedia = 0;
            for (Partido p : eleicao.getPartidos()) {
                if (vagasPorPartido.get(p) >= candidatosDisponiveis.get(p)) continue;
                double media = (double) resultado.votosDoPartido(p) / (vagasPorPartido.get(p) + 1);
                if (media > melhorMedia) { melhorMedia = media; melhor = p; }
            }
            if (melhor == null) break; // ninguém mais pode receber vaga
            vagasPorPartido.merge(melhor, 1, Integer::sum);
            distribuidas++;
        }
        vagasPorPartido.values().removeIf(v -> v == 0);
        resultado.definirVagas(vagasPorPartido);

        List<Candidato> eleitos = new ArrayList<>();
        for (Map.Entry<Partido, Integer> e : vagasPorPartido.entrySet()) {
            resultado.ranking().stream()
                    .filter(c -> c.getPartido().equals(e.getKey()) && apto(resultado, c, minimo))
                    .limit(e.getValue())
                    .forEach(eleitos::add);
        }
        eleitos.sort(Comparator.comparingInt(resultado::votosDe).reversed());
        resultado.definirEleitos(eleitos);
    }

    /** Votação nominal mínima para ocupar uma vaga: 10% do QE e pelo menos 1 voto. */
    private static boolean apto(ResultadoCargo r, Candidato c, int minimo) {
        return r.votosDe(c) >= minimo;
    }
}

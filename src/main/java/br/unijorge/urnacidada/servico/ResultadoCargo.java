package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.Partido;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Totais da apuração de um cargo. */
public class ResultadoCargo {

    private final Cargo cargo;
    private final Map<Candidato, Integer> votosPorCandidato = new LinkedHashMap<>();
    private final Map<Partido, Integer> votosDeLegenda = new LinkedHashMap<>();
    private int brancos;
    private int nulos;

    private final List<Candidato> eleitos = new ArrayList<>();
    private final Map<Partido, Integer> vagasPorPartido = new LinkedHashMap<>();
    private Integer quocienteEleitoral;
    private boolean empate;

    ResultadoCargo(Cargo cargo, List<Candidato> candidatos, List<Partido> partidos) {
        this.cargo = cargo;
        candidatos.forEach(c -> votosPorCandidato.put(c, 0));
        if (cargo.aceitaVotoDeLegenda()) partidos.forEach(p -> votosDeLegenda.put(p, 0));
    }

    void somarCandidato(Candidato c) { votosPorCandidato.merge(c, 1, Integer::sum); }
    void somarLegenda(Partido p) { votosDeLegenda.merge(p, 1, Integer::sum); }
    void somarBranco() { brancos++; }
    void somarNulo() { nulos++; }

    void definirEleitos(List<Candidato> lista) { eleitos.clear(); eleitos.addAll(lista); }
    void definirVagas(Map<Partido, Integer> vagas) { vagasPorPartido.clear(); vagasPorPartido.putAll(vagas); }
    void definirQuocienteEleitoral(Integer qe) { this.quocienteEleitoral = qe; }
    void definirEmpate(boolean empate) { this.empate = empate; }

    public int getVotosNominais() { return votosPorCandidato.values().stream().mapToInt(Integer::intValue).sum(); }
    public int getVotosLegenda() { return votosDeLegenda.values().stream().mapToInt(Integer::intValue).sum(); }
    public int getVotosValidos() { return getVotosNominais() + getVotosLegenda(); }
    public int getBrancos() { return brancos; }
    public int getNulos() { return nulos; }
    public int getComparecimento() { return getVotosValidos() + brancos + nulos; }

    public int votosDe(Candidato c) { return votosPorCandidato.getOrDefault(c, 0); }
    public int legendaDe(Partido p) { return votosDeLegenda.getOrDefault(p, 0); }

    /** Votos do partido = votos nos seus candidatos + votos de legenda. */
    public int votosDoPartido(Partido p) {
        int nominais = votosPorCandidato.entrySet().stream()
                .filter(e -> e.getKey().getPartido().equals(p))
                .mapToInt(Map.Entry::getValue).sum();
        return nominais + legendaDe(p);
    }

    public double percentualValidos(Candidato c) {
        int validos = getVotosValidos();
        return validos == 0 ? 0 : 100.0 * votosDe(c) / validos;
    }

    /** Candidatos do mais votado para o menos votado. */
    public List<Candidato> ranking() {
        return votosPorCandidato.keySet().stream()
                .sorted(Comparator.comparingInt(this::votosDe).reversed()
                        .thenComparing(Candidato::getNome))
                .toList();
    }

    public Cargo getCargo() { return cargo; }
    public List<Candidato> getEleitos() { return List.copyOf(eleitos); }
    public Map<Partido, Integer> getVagasPorPartido() { return Map.copyOf(vagasPorPartido); }
    public Integer getQuocienteEleitoral() { return quocienteEleitoral; }
    public boolean isEmpate() { return empate; }
}

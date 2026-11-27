package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.CargoProporcional;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.Partido;
import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conta os registros de voto e gera o Boletim de Urna (BU) e a Zerésima.
 */
public class Apuracao {

    private final Eleicao eleicao;
    private final Map<Cargo, ResultadoCargo> resultados = new LinkedHashMap<>();
    private int registrosIgnorados;

    public Apuracao(Eleicao eleicao, List<RegistroVoto> registros) {
        this.eleicao = eleicao;
        for (Cargo cargo : eleicao.getCargos()) {
            resultados.put(cargo, new ResultadoCargo(cargo, eleicao.candidatosDo(cargo), eleicao.getPartidos()));
        }
        registros.forEach(this::contar);
        resultados.forEach((cargo, r) -> RegraApuracao.para(cargo).definirEleitos(r, eleicao));
    }

    private void contar(RegistroVoto r) {
        Cargo cargo = eleicao.buscarCargo(r.cargo()).orElse(null);
        if (cargo == null || r.tipo() == null) { registrosIgnorados++; return; }
        ResultadoCargo res = resultados.get(cargo);
        switch (r.tipo()) {
            case BRANCO -> res.somarBranco();
            case NULO -> res.somarNulo();
            case NOMINAL -> eleicao.buscarCandidato(cargo, r.numero())
                    .ifPresentOrElse(res::somarCandidato, res::somarNulo);
            case LEGENDA -> eleicao.buscarPartido(r.numero())
                    .ifPresentOrElse(res::somarLegenda, res::somarNulo);
        }
    }

    public ResultadoCargo resultadoDe(Cargo cargo) { return resultados.get(cargo); }
    public List<ResultadoCargo> getResultados() { return List.copyOf(resultados.values()); }
    public int getRegistrosIgnorados() { return registrosIgnorados; }

    public int getTotalRegistros() {
        return resultados.values().stream().mapToInt(ResultadoCargo::getComparecimento).sum();
    }

    /** A Zerésima é emitida antes da votação e prova que a urna começa sem votos. */
    public String gerarZeresima(String urnaId) {
        StringBuilder sb = cabecalho("ZERÉSIMA", urnaId);
        boolean zerada = getTotalRegistros() == 0;
        for (Cargo cargo : eleicao.getCargos()) {
            sb.append(cargo.getNome().toUpperCase()).append('\n');
            for (Candidato c : eleicao.candidatosDo(cargo)) {
                sb.append(String.format("  %-6s %-22s %5d%n", c.getNumero(), c.getNome(), resultadoDe(cargo).votosDe(c)));
            }
            sb.append('\n');
        }
        sb.append(zerada
                ? "Todos os candidatos estão com ZERO votos.\nA urna está pronta para iniciar a votação.\n"
                : "ATENÇÃO: já existem " + getTotalRegistros() + " registros nesta eleição!\n");
        return sb.toString();
    }

    /** Boletim de Urna: resultado impresso ao final da votação. */
    public String gerarBoletim(String urnaId, int eleitoresQueVotaram) {
        StringBuilder sb = cabecalho("BOLETIM DE URNA", urnaId);
        if (eleitoresQueVotaram >= 0) sb.append("Eleitores que votaram nesta urna: ").append(eleitoresQueVotaram).append("\n\n");
        for (ResultadoCargo r : getResultados()) {
            Cargo cargo = r.getCargo();
            sb.append("=== ").append(cargo.getNome().toUpperCase()).append(" (")
                    .append(cargo.getSistema().getDescricao()).append(") ===\n");
            for (Candidato c : r.ranking()) {
                sb.append(String.format("  %-6s %-22s %-5s %5d  (%5.1f%%)%n", c.getNumero(), c.getNome(),
                        c.getPartido().getSigla(), r.votosDe(c), r.percentualValidos(c)));
            }
            if (cargo.aceitaVotoDeLegenda()) {
                sb.append("  Votos por partido (candidatos + legenda):\n");
                for (Partido p : eleicao.getPartidos()) {
                    sb.append(String.format("    %-5s total %4d  (legenda: %d)%n", p.getSigla(), r.votosDoPartido(p), r.legendaDe(p)));
                }
            }
            sb.append(String.format("  Válidos: %d | Brancos: %d | Nulos: %d | Total: %d%n",
                    r.getVotosValidos(), r.getBrancos(), r.getNulos(), r.getComparecimento()));
            if (r.getQuocienteEleitoral() != null) {
                sb.append("  Quociente eleitoral: ").append(r.getQuocienteEleitoral()).append('\n');
                if (r.getCargo() instanceof CargoProporcional cp && r.getEleitos().size() < cp.getVagas()) {
                    sb.append("  Vagas não preenchidas: ").append(cp.getVagas() - r.getEleitos().size())
                            .append(" (faltaram candidatos com votação mínima)\n");
                }
            }
            if (r.isEmpate()) {
                sb.append("  EMPATE! Pela lei, venceria o candidato mais idoso.\n");
            } else if (!r.getEleitos().isEmpty()) {
                sb.append("  Eleito(s): ");
                sb.append(String.join(", ", r.getEleitos().stream().map(c -> c.getNome() + " (" + c.getPartido().getSigla() + ")").toList()));
                sb.append('\n');
            } else {
                sb.append("  Ainda não há votos válidos.\n");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private StringBuilder cabecalho(String titulo, String urnaId) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  ").append(titulo).append(" – SIMULAÇÃO EDUCATIVA\n");
        sb.append("========================================\n");
        sb.append(eleicao.getTitulo()).append('\n');
        sb.append("Município: ").append(eleicao.getMunicipio()).append('\n');
        sb.append("Eleição: ").append(eleicao.getId()).append(" | Urna: ").append(urnaId).append('\n');
        sb.append("Emitido em: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");
        return sb;
    }
}

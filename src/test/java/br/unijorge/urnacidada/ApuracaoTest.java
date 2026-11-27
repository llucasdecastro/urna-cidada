package br.unijorge.urnacidada;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.RegistroVoto;
import br.unijorge.urnacidada.modelo.TipoVoto;
import br.unijorge.urnacidada.servico.Apuracao;
import br.unijorge.urnacidada.servico.DadosFicticios;
import br.unijorge.urnacidada.servico.ResultadoCargo;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ApuracaoTest {

    private final Eleicao eleicao = DadosFicticios.criarEleicao("teste");

    private static void adicionar(List<RegistroVoto> l, int vezes, String cargo, TipoVoto tipo, String numero, String partido) {
        for (int i = 0; i < vezes; i++) l.add(new RegistroVoto(cargo, tipo, numero, partido, Instant.now(), "u1"));
    }

    private Candidato candidato(String numero) {
        return eleicao.getCandidatos().stream().filter(c -> c.getNumero().equals(numero)).findFirst().orElseThrow();
    }

    @Test
    public void zeresimaSemVotos() {
        Apuracao ap = new Apuracao(eleicao, List.of());
        assertEquals(0, ap.getTotalRegistros());
        assertTrue(ap.gerarZeresima("u1").contains("ZERO votos"));
        assertTrue(ap.resultadoDe(DadosFicticios.PREFEITO).getEleitos().isEmpty());
    }

    @Test
    public void majoritarioVenceMaisVotado() {
        List<RegistroVoto> l = new ArrayList<>();
        adicionar(l, 5, "PREFEITO", TipoVoto.NOMINAL, "21", "PIPA");
        adicionar(l, 3, "PREFEITO", TipoVoto.NOMINAL, "34", "BITS");
        adicionar(l, 4, "PREFEITO", TipoVoto.BRANCO, "", null);
        adicionar(l, 2, "PREFEITO", TipoVoto.NULO, "99", null);
        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.PREFEITO);
        assertEquals(8, r.getVotosValidos());
        assertEquals(4, r.getBrancos());
        assertEquals(2, r.getNulos());
        assertEquals(List.of(candidato("21")), r.getEleitos());
        assertEquals(62.5, r.percentualValidos(candidato("21")), 0.01); // brancos e nulos não contam
    }

    @Test
    public void majoritarioEmpateSemEleito() {
        List<RegistroVoto> l = new ArrayList<>();
        adicionar(l, 2, "PREFEITO", TipoVoto.NOMINAL, "21", "PIPA");
        adicionar(l, 2, "PREFEITO", TipoVoto.NOMINAL, "47", "LER");
        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.PREFEITO);
        assertTrue(r.isEmpate());
        assertTrue(r.getEleitos().isEmpty());
    }

    @Test
    public void proporcionalDistribuiVagasPorPartido() {
        // 90 votos válidos, 9 vagas → QE = 10
        List<RegistroVoto> l = new ArrayList<>();
        adicionar(l, 35, "VEREADOR", TipoVoto.NOMINAL, "21123", "PIPA"); // PIPA: 35 + 5 + 5 legenda = 45
        adicionar(l, 5, "VEREADOR", TipoVoto.NOMINAL, "21456", "PIPA");
        adicionar(l, 5, "VEREADOR", TipoVoto.LEGENDA, "21", "PIPA");
        adicionar(l, 25, "VEREADOR", TipoVoto.NOMINAL, "34111", "BITS"); // BITS: 25
        adicionar(l, 20, "VEREADOR", TipoVoto.NOMINAL, "47777", "LER");  // LER: 20
        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.VEREADOR);

        assertEquals(Integer.valueOf(10), r.getQuocienteEleitoral());
        assertEquals(45, r.votosDoPartido(eleicao.buscarPartido("21").orElseThrow()));
        // Só candidatos com ≥ 10% do QE (1 voto) podem ocupar vaga:
        // PIPA tem 2 aptos, BITS 1, LER 1 → 4 eleitos e 5 vagas sem preencher
        assertEquals(4, r.getEleitos().size());
        assertTrue(r.getEleitos().contains(candidato("21456"))); // eleito com 5 votos graças à força do partido
        assertFalse(r.getEleitos().contains(candidato("34222"))); // 0 votos: não pode ser eleito
        assertFalse(r.getEleitos().contains(candidato("58500")));
    }

    @Test
    public void proporcionalRespeitaLimiteDeVagas() {
        List<RegistroVoto> l = new ArrayList<>();
        for (Candidato c : eleicao.candidatosDo(DadosFicticios.VEREADOR)) {
            adicionar(l, 1, "VEREADOR", TipoVoto.NOMINAL, c.getNumero(), c.getPartido().getSigla());
        }

        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.VEREADOR);
        assertEquals(10, r.getVotosValidos());
        assertEquals(9, r.getEleitos().size());
        assertEquals(9, r.getVagasPorPartido().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(Integer.valueOf(1), r.getVagasPorPartido().get(eleicao.buscarPartido("69").orElseThrow()));
    }

    @Test
    public void proporcionalPreservaPartidoComMaiorMedia() {
        List<RegistroVoto> l = new ArrayList<>();
        for (Candidato c : eleicao.candidatosDo(DadosFicticios.VEREADOR)) {
            adicionar(l, 1, "VEREADOR", TipoVoto.NOMINAL, c.getNumero(), c.getPartido().getSigla());
        }
        adicionar(l, 1, "VEREADOR", TipoVoto.NOMINAL, "69100", "ARTE");

        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.VEREADOR);
        assertEquals(9, r.getEleitos().size());
        assertEquals(Integer.valueOf(2), r.getVagasPorPartido().get(eleicao.buscarPartido("69").orElseThrow()));
        assertEquals(Integer.valueOf(1), r.getVagasPorPartido().get(eleicao.buscarPartido("58").orElseThrow()));
    }

    @Test
    public void votoDeLegendaContaParaOPartidoNaoParaCandidato() {
        List<RegistroVoto> l = new ArrayList<>();
        adicionar(l, 3, "VEREADOR", TipoVoto.LEGENDA, "69", "ARTE");
        ResultadoCargo r = new Apuracao(eleicao, l).resultadoDe(DadosFicticios.VEREADOR);
        assertEquals(3, r.getVotosLegenda());
        assertEquals(0, r.getVotosNominais());
        assertEquals(3, r.legendaDe(eleicao.buscarPartido("69").orElseThrow()));
    }

    @Test
    public void registroDesconhecidoEhIgnorado() {
        List<RegistroVoto> l = new ArrayList<>();
        adicionar(l, 1, "GOVERNADOR", TipoVoto.NOMINAL, "21", "PIPA");
        Apuracao ap = new Apuracao(eleicao, l);
        assertEquals(1, ap.getRegistrosIgnorados());
        assertTrue(ap.gerarBoletim("u1", 0).contains("BOLETIM DE URNA"));
    }
}

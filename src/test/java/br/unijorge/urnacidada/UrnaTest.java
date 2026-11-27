package br.unijorge.urnacidada;

import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.RegistroVoto;
import br.unijorge.urnacidada.modelo.TipoVoto;
import br.unijorge.urnacidada.repositorio.MemoriaVotoRepositorio;
import br.unijorge.urnacidada.servico.DadosFicticios;
import br.unijorge.urnacidada.servico.Previsualizacao.Situacao;
import br.unijorge.urnacidada.servico.Urna;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UrnaTest {

    private MemoriaVotoRepositorio repo;
    private Urna urna;

    @Before
    public void preparar() {
        repo = new MemoriaVotoRepositorio();
        Eleicao e = DadosFicticios.criarEleicao("teste");
        urna = new Urna(e, repo, "urna-teste");
    }

    private void digitar(String numero) {
        for (char c : numero.toCharArray()) urna.digitar(c);
    }

    private RegistroVoto votoDo(String cargo) {
        return repo.listarTodos().stream().filter(r -> r.cargo().equals(cargo)).findFirst().orElseThrow();
    }

    @Test
    public void urnaComecaBloqueadaENaoAceitaDigitos() {
        assertEquals(Urna.Estado.BLOQUEADA, urna.getEstado());
        assertFalse(urna.digitar('1'));
        assertTrue(urna.confirmar().isEmpty());
    }

    @Test
    public void votoNominalNosDoisCargos() {
        urna.liberar();
        assertEquals("Vereador", urna.getCargoAtual().getNome());
        digitar("21123");
        assertEquals(Situacao.CANDIDATO, urna.previsualizar().situacao());
        assertEquals("Ana Raiz", urna.previsualizar().candidato().getNome());
        assertTrue(urna.confirmar().isPresent());

        assertEquals("Prefeito", urna.getCargoAtual().getNome());
        assertTrue(repo.listarTodos().isEmpty()); // só grava no final
        digitar("34");
        assertEquals("Davi Circuito", urna.previsualizar().candidato().getNome());
        urna.confirmar();

        assertEquals(Urna.Estado.FIM, urna.getEstado());
        assertEquals(2, repo.listarTodos().size());
        assertEquals(TipoVoto.NOMINAL, votoDo("VEREADOR").tipo());
        assertEquals("34", votoDo("PREFEITO").numero());
        assertEquals(1, urna.getEleitoresQueVotaram());
    }

    @Test
    public void naoConfirmaComNumeroIncompleto() {
        urna.liberar();
        digitar("2");
        assertTrue(urna.confirmar().isEmpty());
        assertEquals("Vereador", urna.getCargoAtual().getNome());
    }

    @Test
    public void naoDigitaMaisQueOLimiteDoCargo() {
        urna.liberar();
        digitar("2112399");
        assertEquals("21123", urna.previsualizar().digitos());
    }

    @Test
    public void votoDeLegendaComDoisDigitos() {
        urna.liberar();
        digitar("47");
        assertEquals(Situacao.PARTIDO, urna.previsualizar().situacao());
        urna.confirmar();
        digitar("99");
        urna.confirmar();
        RegistroVoto v = votoDo("VEREADOR");
        assertEquals(TipoVoto.LEGENDA, v.tipo());
        assertEquals("LER", v.partido());
    }

    @Test
    public void candidatoInexistenteDePartidoValidoViraLegenda() {
        urna.liberar();
        digitar("58999");
        assertEquals(Situacao.CANDIDATO_INEXISTENTE, urna.previsualizar().situacao());
        urna.confirmar();
        digitar("58");
        urna.confirmar();
        assertEquals(TipoVoto.LEGENDA, votoDo("VEREADOR").tipo());
        assertEquals("MOVA", votoDo("VEREADOR").partido());
    }

    @Test
    public void numeroErradoEhVotoNulo() {
        urna.liberar();
        digitar("99999");
        assertEquals(Situacao.NULO, urna.previsualizar().situacao());
        urna.confirmar();
        digitar("69"); // ARTE não tem candidato a prefeito → nulo (majoritário não tem legenda)
        assertEquals(Situacao.NULO, urna.previsualizar().situacao());
        urna.confirmar();
        assertEquals(TipoVoto.NULO, votoDo("VEREADOR").tipo());
        assertEquals(TipoVoto.NULO, votoDo("PREFEITO").tipo());
    }

    @Test
    public void brancoSoFuncionaSemDigitos() {
        urna.liberar();
        digitar("2");
        assertFalse(urna.votarEmBranco());
        urna.corrigir();
        assertTrue(urna.votarEmBranco());
        assertEquals(Situacao.BRANCO, urna.previsualizar().situacao());
        assertFalse(urna.digitar('1')); // depois do branco não digita
        urna.confirmar();
        urna.votarEmBranco();
        urna.confirmar();
        assertTrue(repo.listarTodos().stream().allMatch(r -> r.tipo() == TipoVoto.BRANCO));
    }

    @Test
    public void corrigeLimpaOsDigitos() {
        urna.liberar();
        digitar("211");
        urna.corrigir();
        assertEquals(Situacao.VAZIO, urna.previsualizar().situacao());
    }

    @Test
    public void ciclodeVariosEleitores() {
        for (int i = 0; i < 3; i++) {
            urna.liberar();
            digitar("34111"); urna.confirmar();
            digitar("21"); urna.confirmar();
            urna.encerrarEleitor();
            assertEquals(Urna.Estado.BLOQUEADA, urna.getEstado());
        }
        assertEquals(3, urna.getEleitoresQueVotaram());
        assertEquals(6, repo.listarTodos().size());
    }

    @Test
    public void votoNaoGuardaDadosDoEleitor() {
        urna.liberar();
        digitar("21123"); urna.confirmar();
        digitar("21"); urna.confirmar();
        List<RegistroVoto> votos = repo.listarTodos();
        // Um registro só tem: cargo, tipo, número, partido, horário e id da urna
        assertEquals(6, RegistroVoto.class.getRecordComponents().length);
        assertEquals("urna-teste", votos.get(0).urnaId());
    }

    @Test(expected = IllegalStateException.class)
    public void naoLiberaDuasVezes() {
        urna.liberar();
        urna.liberar();
    }
}

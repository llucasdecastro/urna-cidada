package br.unijorge.urnacidada;

import br.unijorge.urnacidada.modelo.CandidatoMajoritario;
import br.unijorge.urnacidada.modelo.CandidatoProporcional;
import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.Partido;
import br.unijorge.urnacidada.modelo.VotoLegenda;
import br.unijorge.urnacidada.servico.DadosFicticios;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.*;

public class ModeloTest {

    private final Partido p = new Partido(21, "PIPA", "Partido", "Tema", Color.GREEN);

    @Test
    public void polimorfismoDosCargos() {
        Cargo[] cargos = {DadosFicticios.VEREADOR, DadosFicticios.PREFEITO};
        assertTrue(cargos[0].aceitaVotoDeLegenda());
        assertFalse(cargos[1].aceitaVotoDeLegenda());
        assertEquals(5, cargos[0].getQuantidadeDigitos());
        assertEquals(2, cargos[1].getQuantidadeDigitos());
        assertEquals("VEREADOR", cargos[0].getChave());
    }

    @Test(expected = IllegalArgumentException.class)
    public void numeroDoCandidatoPrecisaComecarComOPartido() {
        new CandidatoProporcional("34123", "X", p, DadosFicticios.VEREADOR, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void numeroDoVereadorPrecisaTer5Digitos() {
        new CandidatoProporcional("211", "X", p, DadosFicticios.VEREADOR, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void majoritarioNaoAceitaLegenda() {
        new VotoLegenda(DadosFicticios.PREFEITO, p);
    }

    @Test
    public void candidatoMajoritarioMostraVice() {
        CandidatoMajoritario c = new CandidatoMajoritario("Clara Ventura", "Marcos Semente", p, DadosFicticios.PREFEITO, null);
        assertEquals("21", c.getNumero());
        assertEquals("Vice-Prefeito: Marcos Semente", c.getInformacaoComplementar());
        assertEquals("CV", c.getIniciais());
    }

    @Test(expected = IllegalArgumentException.class)
    public void partidoPrecisaDeDoisDigitos() {
        new Partido(5, "X", "X", null, null);
    }
}

package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.CandidatoMajoritario;
import br.unijorge.urnacidada.modelo.CandidatoProporcional;
import br.unijorge.urnacidada.modelo.CargoMajoritario;
import br.unijorge.urnacidada.modelo.CargoProporcional;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.Partido;

import java.awt.Color;

/**
 * Monta a eleição simulada usada nas oficinas.
 *
 * <p>TODOS os partidos, siglas, candidatos e propostas são inventados e temáticos,
 * sem qualquer vínculo com legendas ou pessoas reais (projeto apartidário).</p>
 */
public final class DadosFicticios {

    public static final CargoProporcional VEREADOR = new CargoProporcional("Vereador", 5, 9);
    public static final CargoMajoritario PREFEITO = new CargoMajoritario("Prefeito");

    private DadosFicticios() { }

    public static Eleicao criarEleicao(String id) {
        Eleicao e = new Eleicao(id, "Eleição Simulada – Urna Cidadã", "Nova Esperança (município fictício)");

        // Ordem de votação: primeiro o cargo proporcional, depois o majoritário (como na urna real)
        e.adicionarCargo(VEREADOR);
        e.adicionarCargo(PREFEITO);

        Partido pipa = new Partido(21, "PIPA", "Partido Independente dos Parques e Árvores", "Meio ambiente", new Color(46, 139, 87));
        Partido bits = new Partido(34, "BITS", "Bloco de Inovação e Tecnologia Social", "Tecnologia", new Color(52, 101, 164));
        Partido ler = new Partido(47, "LER", "Liga de Estímulo à Leitura", "Educação", new Color(196, 120, 20));
        Partido mova = new Partido(58, "MOVA", "Movimento pelo Esporte e Vida Ativa", "Esporte", new Color(192, 57, 43));
        Partido arte = new Partido(69, "ARTE", "Aliança pela Arte, Ritmo e Expressão", "Cultura", new Color(142, 68, 173));
        for (Partido p : new Partido[]{pipa, bits, ler, mova, arte}) e.adicionarPartido(p);

        // Vereadores (5 dígitos, começando pelo número do partido)
        e.adicionarCandidato(new CandidatoProporcional("21123", "Ana Raiz", pipa, VEREADOR, "Uma horta comunitária em cada bairro"));
        e.adicionarCandidato(new CandidatoProporcional("21456", "Beto Folhagem", pipa, VEREADOR, "Mais árvores e sombra nos pontos de ônibus"));
        e.adicionarCandidato(new CandidatoProporcional("34111", "Carla Pixel", bits, VEREADOR, "Wi-Fi gratuito nas praças"));
        e.adicionarCandidato(new CandidatoProporcional("34222", "Diego Byte", bits, VEREADOR, "Cursos de programação nas escolas"));
        e.adicionarCandidato(new CandidatoProporcional("47777", "Elisa Verso", ler, VEREADOR, "Bibliotecas abertas também à noite"));
        e.adicionarCandidato(new CandidatoProporcional("47010", "Fábio Capítulo", ler, VEREADOR, "Clube de leitura em todo posto de saúde"));
        e.adicionarCandidato(new CandidatoProporcional("58500", "Gabi Passada", mova, VEREADOR, "Quadras iluminadas e reformadas"));
        e.adicionarCandidato(new CandidatoProporcional("58123", "Hugo Drible", mova, VEREADOR, "Campeonato escolar municipal"));
        e.adicionarCandidato(new CandidatoProporcional("69100", "Íris Batuque", arte, VEREADOR, "Palco aberto para artistas locais"));
        e.adicionarCandidato(new CandidatoProporcional("69369", "João Grafite", arte, VEREADOR, "Muros da cidade viram galerias"));

        // Prefeitos (2 dígitos = número do partido), cada um com vice
        e.adicionarCandidato(new CandidatoMajoritario("Clara Ventura", "Marcos Semente", pipa, PREFEITO, "Cidade mais verde e com menos calor"));
        e.adicionarCandidato(new CandidatoMajoritario("Davi Circuito", "Lia Nuvem", bits, PREFEITO, "Serviços públicos pelo celular"));
        e.adicionarCandidato(new CandidatoMajoritario("Helena Páginas", "Otávio Rima", ler, PREFEITO, "Escola em tempo integral"));
        e.adicionarCandidato(new CandidatoMajoritario("Rui Maratona", "Sofia Saque", mova, PREFEITO, "Esporte e lazer em todos os bairros"));

        return e;
    }
}

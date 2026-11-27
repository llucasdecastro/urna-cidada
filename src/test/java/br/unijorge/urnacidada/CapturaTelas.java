package br.unijorge.urnacidada;

import br.unijorge.urnacidada.repositorio.MemoriaVotoRepositorio;
import br.unijorge.urnacidada.servico.Apuracao;
import br.unijorge.urnacidada.servico.DadosFicticios;
import br.unijorge.urnacidada.servico.Urna;
import br.unijorge.urnacidada.ui.DialogoResultados;
import br.unijorge.urnacidada.ui.JanelaPrincipal;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilitário de documentação: abre a janela, simula uma votação e salva
 * capturas de tela em docs/telas/. Uso: java ... CapturaTelas docs/telas
 */
public class CapturaTelas {

    public static void main(String[] args) throws Exception {
        File pasta = new File(args.length > 0 ? args[0] : "docs/telas");
        pasta.mkdirs();
        MemoriaVotoRepositorio repo = new MemoriaVotoRepositorio();
        Urna urna = new Urna(DadosFicticios.criarEleicao("oficina-2026"), repo, "urna-01");
        JanelaPrincipal[] j = new JanelaPrincipal[1];
        SwingUtilities.invokeAndWait(() -> { j[0] = new JanelaPrincipal(urna, "1234"); j[0].setVisible(true); });
        Thread.sleep(800);

        salvar(j[0], pasta, "01-aguardando.png");
        executar(urna::liberar);
        salvar(j[0], pasta, "02-vereador-vazio.png");
        digitar(urna, "21123");
        salvar(j[0], pasta, "03-vereador-candidato.png");
        executar(urna::corrigir);
        digitar(urna, "47");
        salvar(j[0], pasta, "04-vereador-legenda.png");
        executar(urna::corrigir);
        digitar(urna, "99999");
        salvar(j[0], pasta, "05-voto-nulo.png");
        executar(urna::corrigir);
        digitar(urna, "21123");
        executar(urna::confirmar);
        digitar(urna, "34");
        salvar(j[0], pasta, "06-prefeito-candidato.png");
        executar(urna::corrigir);
        executar(urna::votarEmBranco);
        salvar(j[0], pasta, "07-branco.png");
        executar(urna::confirmar);
        salvar(j[0], pasta, "08-fim.png");

        // Mais alguns eleitores para a apuração ter dados
        String[][] votos = {{"21123", "21"}, {"34111", "34"}, {"34222", "34"}, {"47", "47"}, {"58500", "21"},
                {"21456", "21"}, {"69100", "58"}, {"34111", "34"}, {"99999", ""}, {"21123", "21"}};
        for (String[] v : votos) {
            executar(urna::encerrarEleitor);
            executar(urna::liberar);
            digitar(urna, v[0]); executar(urna::confirmar);
            if (v[1].isEmpty()) executar(urna::votarEmBranco); else digitar(urna, v[1]);
            executar(urna::confirmar);
        }
        executar(urna::encerrarEleitor);

        Apuracao ap = new Apuracao(urna.getEleicao(), repo.listarTodos());
        DialogoResultados[] d = new DialogoResultados[1];
        SwingUtilities.invokeAndWait(() -> {
            d[0] = new DialogoResultados(j[0], "Apuração", ap, ap.gerarBoletim("urna-01", urna.getEleitoresQueVotaram()), true);
            d[0].setModal(false);
            d[0].setVisible(true);
        });
        Thread.sleep(600);
        salvar(d[0], pasta, "09-apuracao.png");
        System.out.println(ap.gerarBoletim("urna-01", urna.getEleitoresQueVotaram()));
        System.exit(0);
    }

    private static void digitar(Urna u, String n) throws Exception {
        for (char c : n.toCharArray()) executar(() -> u.digitar(c));
    }

    private static void executar(Runnable r) throws Exception {
        SwingUtilities.invokeAndWait(r);
    }

    private static void salvar(Component c, File pasta, String nome) throws Exception {
        Thread.sleep(300);
        BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            if (c instanceof JFrame f) f.getRootPane().paint(img.getGraphics());
            else if (c instanceof javax.swing.JDialog dd) dd.getRootPane().paint(img.getGraphics());
            else c.paint(img.getGraphics());
        });
        ImageIO.write(img, "png", new File(pasta, nome));
    }
}

package br.unijorge.urnacidada.ui;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.servico.ResultadoCargo;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/** Gráfico de barras horizontal com o resultado de um cargo (para projetar na oficina). */
public class PainelGrafico extends JPanel {

    private final ResultadoCargo resultado;

    public PainelGrafico(ResultadoCargo resultado) {
        this.resultado = resultado;
        int linhas = resultado.ranking().size() + 2;
        setPreferredSize(new Dimension(620, 70 + linhas * 30));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x0 = 190, larguraMax = getWidth() - x0 - 90, y = 40;
        g.setColor(new Color(0x22, 0x22, 0x22));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.drawString(resultado.getCargo().getNome() + " – " + resultado.getCargo().getSistema().getDescricao(), 16, 24);

        List<Candidato> ranking = resultado.ranking();
        int maximo = Math.max(1, Math.max(resultado.getBrancos(), Math.max(resultado.getNulos(),
                ranking.isEmpty() ? 0 : resultado.votosDe(ranking.get(0)))));

        for (Candidato c : ranking) {
            boolean eleito = resultado.getEleitos().contains(c);
            barra(g, c.getNome() + " (" + c.getPartido().getSigla() + ")", resultado.votosDe(c), maximo,
                    c.getPartido().getCor(), x0, y, larguraMax, eleito);
            y += 30;
        }
        barra(g, "Brancos", resultado.getBrancos(), maximo, new Color(0xBB, 0xBB, 0xBB), x0, y, larguraMax, false);
        y += 30;
        barra(g, "Nulos", resultado.getNulos(), maximo, new Color(0x88, 0x88, 0x88), x0, y, larguraMax, false);
    }

    private void barra(Graphics2D g, String rotulo, int votos, int maximo, Color cor, int x0, int y, int larguraMax, boolean eleito) {
        g.setFont(new Font(Font.SANS_SERIF, eleito ? Font.BOLD : Font.PLAIN, 13));
        g.setColor(new Color(0x33, 0x33, 0x33));
        g.drawString(rotulo, 16, y + 15);
        int largura = (int) Math.round((double) votos / maximo * larguraMax);
        g.setColor(cor);
        g.fillRoundRect(x0, y + 2, Math.max(largura, 2), 18, 6, 6);
        g.setColor(new Color(0x33, 0x33, 0x33));
        g.drawString(votos + (eleito ? "  ✔ eleito" : ""), x0 + largura + 8, y + 15);
    }
}

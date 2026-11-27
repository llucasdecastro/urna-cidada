package br.unijorge.urnacidada.ui;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Partido;
import br.unijorge.urnacidada.servico.Previsualizacao;
import br.unijorge.urnacidada.servico.Urna;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Desenha a tela da urna, imitando o layout da urna eletrônica brasileira.
 * Só lê o estado da {@link Urna}; não altera nada (separação entre visão e regra).
 */
public class TelaUrna extends JPanel {

    private static final Color FUNDO = new Color(0xF2, 0xF2, 0xEC);
    private static final Color TEXTO = new Color(0x1E, 0x1E, 0x1E);
    private static final Color CINZA = new Color(0x70, 0x70, 0x70);
    private static final Color DICA = new Color(0x1F, 0x5F, 0xA8);
    private static final String FONTE = Font.SANS_SERIF;

    private final Urna urna;
    private boolean modoOficina = true;
    private String mensagemRodape;

    public TelaUrna(Urna urna) {
        this.urna = urna;
        setPreferredSize(new Dimension(680, 460));
        setBackground(FUNDO);
    }

    public void setModoOficina(boolean modoOficina) { this.modoOficina = modoOficina; repaint(); }
    public boolean isModoOficina() { return modoOficina; }
    public void setMensagemRodape(String msg) { this.mensagemRodape = msg; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setColor(FUNDO);
        g.fillRect(0, 0, w, h);

        switch (urna.getEstado()) {
            case BLOQUEADA -> desenharAguardando(g, w, h);
            case FIM -> desenharFim(g, w, h);
            case VOTANDO -> desenharVotacao(g, w, h);
        }
        if (mensagemRodape != null) {
            g.setFont(new Font(FONTE, Font.PLAIN, 12));
            g.setColor(new Color(0xB0, 0x3A, 0x2E));
            g.drawString(mensagemRodape, 12, h - 8);
        }
        g.dispose();
    }

    private void desenharAguardando(Graphics2D g, int w, int h) {
        g.setColor(TEXTO);
        centralizado(g, "URNA CIDADÃ", new Font(FONTE, Font.BOLD, 44), w, h / 2 - 70);
        g.setColor(CINZA);
        centralizado(g, "Simulação educativa – candidatos e partidos fictícios", new Font(FONTE, Font.PLAIN, 15), w, h / 2 - 35);
        g.setColor(TEXTO);
        centralizado(g, "AGUARDANDO LIBERAÇÃO", new Font(FONTE, Font.BOLD, 26), w, h / 2 + 25);
        g.setColor(CINZA);
        centralizado(g, "Mesário: clique em \"Liberar eleitor\" (ou tecle F2)", new Font(FONTE, Font.PLAIN, 15), w, h / 2 + 55);
        centralizado(g, "Eleitores que já votaram: " + urna.getEleitoresQueVotaram(), new Font(FONTE, Font.PLAIN, 14), w, h - 60);
        centralizado(g, "Votos gravados em: " + urna.getRepositorio().getDescricao(), new Font(FONTE, Font.PLAIN, 12), w, h - 38);
    }

    private void desenharFim(Graphics2D g, int w, int h) {
        g.setColor(TEXTO);
        centralizado(g, "FIM", new Font(FONTE, Font.BOLD, 150), w, h / 2 + 50);
        g.setColor(CINZA);
        centralizado(g, "VOTOU", new Font(FONTE, Font.BOLD, 18), w, h - 50);
    }

    private void desenharVotacao(Graphics2D g, int w, int h) {
        Previsualizacao p = urna.previsualizar();
        int x = 28;

        g.setColor(TEXTO);
        g.setFont(new Font(FONTE, Font.BOLD, 15));
        g.drawString("SEU VOTO PARA", x, 34);
        g.setFont(new Font(FONTE, Font.PLAIN, 12));
        g.setColor(CINZA);
        String etapa = "Cargo " + urna.getPosicaoCargo() + " de " + urna.getTotalCargos();
        g.drawString(etapa, w - 28 - g.getFontMetrics().stringWidth(etapa), 34);

        g.setColor(TEXTO);
        centralizado(g, p.cargo().getNome(), new Font(FONTE, Font.BOLD, 32), w - 170, 80);

        if (p.situacao() == Previsualizacao.Situacao.BRANCO) {
            centralizado(g, "VOTO EM BRANCO", new Font(FONTE, Font.BOLD, 44), w, h / 2 + 20);
            if (modoOficina) dica(g, w, h, "O voto em branco não vai para nenhum candidato e não entra nos votos válidos.");
            rodapeConfirmacao(g, w, h);
            return;
        }

        // Número com uma caixinha para cada dígito
        int y = 140;
        g.setFont(new Font(FONTE, Font.PLAIN, 18));
        g.drawString("Número:", x, y);
        int qtd = p.cargo().getQuantidadeDigitos();
        int caixa = 38, bx = x + 90;
        for (int i = 0; i < qtd; i++) {
            int cx = bx + i * (caixa + 6);
            g.setColor(Color.WHITE);
            g.fillRect(cx, y - 30, caixa, caixa + 6);
            g.setColor(TEXTO);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRect(cx, y - 30, caixa, caixa + 6);
            if (i < p.digitos().length()) {
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
                g.drawString(String.valueOf(p.digitos().charAt(i)), cx + 10, y + 3);
            } else if (i == p.digitos().length() && (System.currentTimeMillis() / 500) % 2 == 0) {
                g.fillRect(cx + 8, y + 2, caixa - 16, 3); // cursor piscando
            }
        }

        int ly = y + 50;
        Font rotulo = new Font(FONTE, Font.PLAIN, 18);
        Font valor = new Font(FONTE, Font.BOLD, 18);
        switch (p.situacao()) {
            case CANDIDATO -> {
                Candidato c = p.candidato();
                int limite = w - 150 - 20; // não invadir o retrato
                linha(g, "Nome:", c.getNome(), x, ly, rotulo, valor, limite);
                linha(g, "Partido:", c.getPartido().getSigla() + " – " + c.getPartido().getNome(), x, ly + 32, rotulo, valor, limite);
                if (c.getInformacaoComplementar() != null) {
                    g.setFont(rotulo);
                    g.drawString(c.getInformacaoComplementar(), x, ly + 64);
                }
                retrato(g, c, w - 150, 105);
                if (modoOficina && c.getBandeira() != null) dica(g, w, h, "Proposta (fictícia): " + c.getBandeira());
                rodapeConfirmacao(g, w, h);
            }
            case PARTIDO -> {
                partido(g, p.partido(), x, ly, rotulo, valor);
                if (modoOficina) dica(g, w, h, "Continue digitando o número do candidato, ou CONFIRMA para votar só no partido (legenda).");
                rodapeConfirmacao(g, w, h);
            }
            case CANDIDATO_INEXISTENTE -> {
                g.setFont(new Font(FONTE, Font.BOLD, 22));
                g.drawString("CANDIDATO INEXISTENTE", x, ly);
                partido(g, p.partido(), x, ly + 34, rotulo, valor);
                g.setFont(new Font(FONTE, Font.BOLD, 26));
                g.drawString("VOTO DE LEGENDA", x, ly + 80);
                if (modoOficina) dica(g, w, h, "Esse candidato não existe, mas o partido sim: o voto vai para o partido.");
                rodapeConfirmacao(g, w, h);
            }
            case NULO -> {
                g.setFont(new Font(FONTE, Font.BOLD, 24));
                g.drawString("NÚMERO ERRADO", x, ly + 10);
                g.setFont(new Font(FONTE, Font.BOLD, 40));
                g.drawString("VOTO NULO", x, ly + 66);
                if (modoOficina) dica(g, w, h, "Voto nulo não vai para ninguém. Aperte CORRIGE se digitou errado.");
                rodapeConfirmacao(g, w, h);
            }
            case VAZIO -> {
                if (modoOficina) dica(g, w, h, "Digite o número do seu candidato no teclado. Se não quiser escolher ninguém, aperte BRANCO.");
            }
            case DIGITANDO -> {
                if (modoOficina) dica(g, w, h, "Confira o número. Para recomeçar, aperte CORRIGE.");
            }
            default -> { }
        }
    }

    private void partido(Graphics2D g, Partido partido, int x, int y, Font rotulo, Font valor) {
        linha(g, "Partido:", partido.getSigla() + " – " + partido.getNome(), x, y, rotulo, valor, getWidth() - 20);
    }

    /** Escreve "rótulo valor", reduzindo a fonte do valor se ele não couber até {@code limite}. */
    private void linha(Graphics2D g, String r, String v, int x, int y, Font rotulo, Font valor, int limite) {
        g.setColor(TEXTO);
        g.setFont(rotulo);
        g.drawString(r, x, y);
        Font f = valor;
        while (f.getSize() > 11 && g.getFontMetrics(f).stringWidth(v) > limite - (x + 90)) {
            f = f.deriveFont(f.getSize2D() - 1f);
        }
        g.setFont(f);
        g.drawString(v, x + 90, y);
    }

    /** Não usamos fotos de pessoas: desenhamos um retrato com as iniciais e a cor do partido. */
    private void retrato(Graphics2D g, Candidato c, int x, int y) {
        int lw = 118, lh = 150;
        g.setColor(Color.WHITE);
        g.fillRect(x, y, lw, lh);
        g.setColor(c.getPartido().getCor());
        g.fillOval(x + 24, y + 18, 70, 70);
        g.fillRoundRect(x + 14, y + 96, lw - 28, lh - 100, 36, 36);
        g.setColor(Color.WHITE);
        g.setFont(new Font(FONTE, Font.BOLD, 26));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(c.getIniciais(), x + 59 - fm.stringWidth(c.getIniciais()) / 2, y + 63);
        g.setColor(TEXTO);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(x, y, lw, lh);
        g.setFont(new Font(FONTE, Font.PLAIN, 11));
        g.drawString(c.getCargo().getNome(), x, y + lh + 14);
    }

    private void dica(Graphics2D g, int w, int h, String texto) {
        g.setColor(DICA);
        g.setFont(new Font(FONTE, Font.ITALIC, 13));
        g.drawString("Dica: " + texto, 28, h - 118);
    }

    private void rodapeConfirmacao(Graphics2D g, int w, int h) {
        int y = h - 98;
        g.setColor(TEXTO);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(16, y, w - 16, y);
        g.setFont(new Font(FONTE, Font.PLAIN, 15));
        g.drawString("Aperte a tecla:", 28, y + 24);
        g.setFont(new Font(FONTE, Font.BOLD, 15));
        g.drawString("CONFIRMA", 70, y + 48);
        g.drawString("CORRIGE", 70, y + 70);
        g.setFont(new Font(FONTE, Font.PLAIN, 15));
        g.drawString("para CONFIRMAR este voto", 160, y + 48);
        g.drawString("para REINICIAR este voto", 160, y + 70);
    }

    private static void centralizado(Graphics2D g, String s, Font f, int w, int y) {
        g.setFont(f);
        g.drawString(s, (w - g.getFontMetrics().stringWidth(s)) / 2, y);
    }
}

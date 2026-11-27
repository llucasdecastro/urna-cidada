package br.unijorge.urnacidada.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** Teclado numérico da urna com as teclas BRANCO, CORRIGE e CONFIRMA. */
public class TecladoUrna extends JPanel {

    public enum Tecla { BRANCO, CORRIGE, CONFIRMA }

    private static final Color CORPO = new Color(0x2B, 0x2B, 0x2B);

    public TecladoUrna(Consumer<Character> aoDigitar, Consumer<Tecla> aoTeclaEspecial) {
        super(new BorderLayout(0, 14));
        setBackground(CORPO);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        JLabel titulo = new JLabel("JUSTIÇA ELEITORAL  ·  SIMULAÇÃO", SwingConstants.CENTER);
        titulo.setForeground(new Color(0xCC, 0xCC, 0xCC));
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        add(titulo, BorderLayout.NORTH);

        JPanel numeros = new JPanel(new GridLayout(4, 3, 10, 10));
        numeros.setOpaque(false);
        for (char c : "123456789".toCharArray()) numeros.add(botaoNumero(c, aoDigitar));
        numeros.add(vazio());
        numeros.add(botaoNumero('0', aoDigitar));
        numeros.add(vazio());
        add(numeros, BorderLayout.CENTER);

        JPanel especiais = new JPanel(new GridLayout(1, 3, 8, 0));
        especiais.setOpaque(false);
        especiais.add(botaoEspecial("BRANCO", Color.WHITE, Color.BLACK, Tecla.BRANCO, aoTeclaEspecial));
        especiais.add(botaoEspecial("CORRIGE", new Color(0xE8, 0x7A, 0x1E), Color.BLACK, Tecla.CORRIGE, aoTeclaEspecial));
        JButton confirma = botaoEspecial("CONFIRMA", new Color(0x2E, 0x9E, 0x4F), Color.BLACK, Tecla.CONFIRMA, aoTeclaEspecial);
        confirma.setPreferredSize(new Dimension(96, 66));
        especiais.add(confirma);
        add(especiais, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(330, 460));
    }

    private JButton botaoNumero(char c, Consumer<Character> acao) {
        JButton b = base(String.valueOf(c), new Color(0x1A, 0x1A, 0x1A), Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        b.addActionListener(e -> acao.accept(c));
        return b;
    }

    private JButton botaoEspecial(String texto, Color fundo, Color frente, Tecla t, Consumer<Tecla> acao) {
        JButton b = base(texto, fundo, frente);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        b.setPreferredSize(new Dimension(90, 58));
        b.addActionListener(e -> acao.accept(t));
        return b;
    }

    private JButton base(String texto, Color fundo, Color frente) {
        JButton b = new JButton(texto);
        b.setBackground(fundo);
        b.setForeground(frente);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setFocusable(false); // o foco fica na janela, para o teclado físico funcionar
        b.setBorder(BorderFactory.createLineBorder(new Color(0x55, 0x55, 0x55), 2, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel vazio() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }
}

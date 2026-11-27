package br.unijorge.urnacidada.ui;

import br.unijorge.urnacidada.servico.Apuracao;
import br.unijorge.urnacidada.servico.ResultadoCargo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Mostra a apuração: gráficos por cargo + Boletim de Urna em texto. */
public class DialogoResultados extends JDialog {

    public DialogoResultados(JFrame dono, String titulo, Apuracao apuracao, String textoBoletim, boolean mostrarGraficos) {
        super(dono, titulo, true);
        JTabbedPane abas = new JTabbedPane();

        if (mostrarGraficos) {
            JPanel graficos = new JPanel();
            graficos.setLayout(new BoxLayout(graficos, BoxLayout.Y_AXIS));
            graficos.setBackground(Color.WHITE);
            for (ResultadoCargo r : apuracao.getResultados()) {
                graficos.add(new PainelGrafico(r));
                JTextArea regra = new JTextArea(r.getCargo().explicacaoDaRegra());
                regra.setLineWrap(true);
                regra.setWrapStyleWord(true);
                regra.setEditable(false);
                ((javax.swing.text.DefaultCaret) regra.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
                regra.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                regra.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
                regra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                graficos.add(regra);
            }
            JScrollPane rolagem = new JScrollPane(graficos);
            rolagem.getVerticalScrollBar().setUnitIncrement(16);
            abas.addTab("Gráficos", rolagem);
            javax.swing.SwingUtilities.invokeLater(() -> rolagem.getVerticalScrollBar().setValue(0));
        }

        JTextArea texto = new JTextArea(textoBoletim);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        texto.setEditable(false);
        texto.setCaretPosition(0);
        texto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        abas.addTab(mostrarGraficos ? "Boletim de Urna" : titulo, new JScrollPane(texto));

        JButton salvar = new JButton("Salvar em .txt");
        salvar.addActionListener(e -> salvar(textoBoletim));
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(salvar);
        botoes.add(fechar);

        setLayout(new BorderLayout());
        add(abas, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        setSize(720, 640);
        setLocationRelativeTo(dono);
    }

    private void salvar(String conteudo) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("boletim-urna.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(fc.getSelectedFile().toPath(), conteudo, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
            }
        }
    }
}

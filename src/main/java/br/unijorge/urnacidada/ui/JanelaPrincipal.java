package br.unijorge.urnacidada.ui;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.RegistroVoto;
import br.unijorge.urnacidada.repositorio.RepositorioComContingencia;
import br.unijorge.urnacidada.repositorio.RepositorioException;
import br.unijorge.urnacidada.servico.Apuracao;
import br.unijorge.urnacidada.servico.Urna;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Janela principal: barra do mesário em cima, tela da urna à esquerda e
 * teclado à direita. Também aceita o teclado do computador
 * (números, Backspace = CORRIGE, B = BRANCO, Enter = CONFIRMA).
 */
public class JanelaPrincipal extends JFrame {

    private final Urna urna;
    private final String pinMesario;
    private final TelaUrna tela;
    private final JLabel status = new JLabel();
    private final JButton btnLiberar = new JButton("Liberar eleitor (F2)");
    private final JButton btnSincronizar = new JButton("Enviar pendentes");

    public JanelaPrincipal(Urna urna, String pinMesario) {
        super("Urna Cidadã – Simulador Educativo de Votação");
        this.urna = urna;
        this.pinMesario = pinMesario;
        this.tela = new TelaUrna(urna);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(criarBarraMesario(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(18, 0));
        corpo.setBackground(new Color(0x2B, 0x2B, 0x2B));
        corpo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 0));
        JPanel moldura = new JPanel(new BorderLayout());
        moldura.setBorder(BorderFactory.createLineBorder(new Color(0x11, 0x11, 0x11), 8));
        moldura.add(tela);
        corpo.add(moldura, BorderLayout.CENTER);
        corpo.add(new TecladoUrna(this::digitar, this::teclaEspecial), BorderLayout.EAST);
        add(corpo, BorderLayout.CENTER);

        configurarAtalhos();
        // Faz o cursor piscar na tela
        new Timer(500, e -> { if (urna.getEstado() == Urna.Estado.VOTANDO) tela.repaint(); }).start();

        atualizar();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private JPanel criarBarraMesario() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        barra.setBackground(new Color(0x1F, 0x3A, 0x5F));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botoes.setOpaque(false);
        JLabel mesario = new JLabel("MESÁRIO:");
        mesario.setForeground(Color.WHITE);
        mesario.setFont(mesario.getFont().deriveFont(Font.BOLD));
        botoes.add(mesario);

        btnLiberar.addActionListener(e -> liberar());
        botoes.add(btnLiberar);

        JButton zeresima = new JButton("Zerésima");
        zeresima.setToolTipText("Relatório emitido ANTES da votação, provando que a urna está zerada");
        zeresima.addActionListener(e -> comPin(() -> abrirRelatorio(true)));
        botoes.add(zeresima);

        JButton apuracao = new JButton("Apuração / Boletim");
        apuracao.addActionListener(e -> comPin(() -> abrirRelatorio(false)));
        botoes.add(apuracao);

        JButton candidatos = new JButton("Lista de candidatos");
        candidatos.setToolTipText("A \"colinha\" com os números é permitida na cabine de votação");
        candidatos.addActionListener(e -> mostrarCandidatos());
        botoes.add(candidatos);

        if (urna.getRepositorio() instanceof RepositorioComContingencia) {
            btnSincronizar.addActionListener(e -> sincronizar());
            botoes.add(btnSincronizar);
        }

        JCheckBox oficina = new JCheckBox("Modo oficina (dicas)", true);
        oficina.setOpaque(false);
        oficina.setForeground(Color.WHITE);
        oficina.setFocusable(false);
        oficina.addActionListener(e -> tela.setModoOficina(oficina.isSelected()));
        botoes.add(oficina);

        for (var c : botoes.getComponents()) c.setFocusable(false);
        barra.add(botoes, BorderLayout.WEST);

        status.setForeground(new Color(0xDD, 0xE6, 0xF0));
        barra.add(status, BorderLayout.EAST);
        return barra;
    }

    private void configurarAtalhos() {
        JComponent raiz = getRootPane();
        for (char c = '0'; c <= '9'; c++) {
            final char d = c;
            atalho(raiz, String.valueOf(c), "dig" + c, () -> digitar(d));
            atalho(raiz, "NUMPAD" + c, "num" + c, () -> digitar(d));
        }
        atalho(raiz, "BACK_SPACE", "corrige", () -> teclaEspecial(TecladoUrna.Tecla.CORRIGE));
        atalho(raiz, "B", "branco", () -> teclaEspecial(TecladoUrna.Tecla.BRANCO));
        atalho(raiz, "ENTER", "confirma", () -> teclaEspecial(TecladoUrna.Tecla.CONFIRMA));
        atalho(raiz, "F2", "liberar", this::liberar);
    }

    private void atalho(JComponent c, String tecla, String nome, Runnable acao) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nome);
        c.getActionMap().put(nome, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { acao.run(); }
        });
    }

    // ------------------------------------------------------------------ ações

    private void liberar() {
        if (urna.getEstado() != Urna.Estado.BLOQUEADA) return;
        urna.liberar();
        tela.setMensagemRodape(null);
        atualizar();
    }

    private void digitar(char d) {
        if (urna.digitar(d)) {
            Som.tecla();
            tela.repaint();
        }
    }

    private void teclaEspecial(TecladoUrna.Tecla t) {
        if (urna.getEstado() != Urna.Estado.VOTANDO) return;
        switch (t) {
            case CORRIGE -> { urna.corrigir(); Som.tecla(); }
            case BRANCO -> { if (urna.votarEmBranco()) Som.tecla(); }
            case CONFIRMA -> confirmar();
        }
        tela.repaint();
    }

    private void confirmar() {
        try {
            if (urna.confirmar().isEmpty()) return;
        } catch (RepositorioException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível gravar os votos:\n" + ex.getMessage(),
                    "Erro de gravação", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (urna.getEstado() == Urna.Estado.FIM) {
            Som.fim();
            if (urna.getRepositorio() instanceof RepositorioComContingencia r && r.getUltimoErro() != null) {
                tela.setMensagemRodape("Sem conexão com a nuvem: voto guardado na contingência local.");
            }
            Timer t = new Timer(2500, e -> { urna.encerrarEleitor(); atualizar(); });
            t.setRepeats(false);
            t.start();
        } else {
            Som.confirma();
        }
        atualizar();
    }

    private void atualizar() {
        btnLiberar.setEnabled(urna.getEstado() == Urna.Estado.BLOQUEADA);
        String s = "Eleição: " + urna.getEleicao().getId() + "  |  " + urna.getUrnaId()
                + "  |  Votaram: " + urna.getEleitoresQueVotaram();
        if (urna.getRepositorio() instanceof RepositorioComContingencia r) {
            int pend = r.getQuantidadePendentes();
            btnSincronizar.setEnabled(pend > 0);
            if (pend > 0) s += "  |  Pendentes: " + pend;
        }
        status.setText(s);
        tela.repaint();
    }

    private void comPin(Runnable acao) {
        JPasswordField campo = new JPasswordField(8);
        int r = JOptionPane.showConfirmDialog(this, new Object[]{"PIN do mesário:", campo}, "Acesso do mesário",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        if (pinMesario.equals(new String(campo.getPassword()))) {
            acao.run();
        } else {
            JOptionPane.showMessageDialog(this, "PIN incorreto.", "Acesso negado", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Busca os votos fora da thread da interface (a nuvem pode demorar). */
    private void abrirRelatorio(boolean zeresima) {
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        carregarVotos(registros -> {
            setCursor(java.awt.Cursor.getDefaultCursor());
            Apuracao ap = new Apuracao(urna.getEleicao(), registros);
            String texto = zeresima
                    ? ap.gerarZeresima(urna.getUrnaId())
                    : ap.gerarBoletim(urna.getUrnaId(), urna.getEleitoresQueVotaram());
            String aviso = urna.getRepositorio() instanceof RepositorioComContingencia r && r.getUltimoErro() != null
                    ? "\nATENÇÃO: sem conexão com a nuvem. Mostrando só os votos locais.\n(" + r.getUltimoErro() + ")\n" : "";
            new DialogoResultados(this, zeresima ? "Zerésima" : "Apuração", ap, texto + aviso, !zeresima).setVisible(true);
        });
    }

    private void carregarVotos(Consumer<List<RegistroVoto>> depois) {
        new SwingWorker<List<RegistroVoto>, Void>() {
            @Override protected List<RegistroVoto> doInBackground() { return urna.getRepositorio().listarTodos(); }
            @Override protected void done() {
                try {
                    depois.accept(get());
                } catch (Exception ex) {
                    setCursor(java.awt.Cursor.getDefaultCursor());
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(JanelaPrincipal.this, "Erro ao ler os votos:\n" + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void sincronizar() {
        if (!(urna.getRepositorio() instanceof RepositorioComContingencia r)) return;
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() { return r.sincronizarPendentes(); }
            @Override protected void done() {
                try {
                    int n = get();
                    String msg = n + " voto(s) enviados para a nuvem.";
                    if (r.getQuantidadePendentes() > 0) msg += "\nAinda pendentes: " + r.getQuantidadePendentes() + "\n" + r.getUltimoErro();
                    JOptionPane.showMessageDialog(JanelaPrincipal.this, msg);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JanelaPrincipal.this, "Erro: " + ex.getMessage());
                }
                atualizar();
            }
        }.execute();
    }

    private void mostrarCandidatos() {
        StringBuilder sb = new StringBuilder("Todos os partidos e candidatos são FICTÍCIOS.\n\n");
        for (Cargo cargo : urna.getEleicao().getCargos()) {
            sb.append(cargo.getNome().toUpperCase()).append(" (").append(cargo.getQuantidadeDigitos()).append(" dígitos)\n");
            for (Candidato c : urna.getEleicao().candidatosDo(cargo)) {
                sb.append(String.format("  %-6s %-18s %-5s %s%n", c.getNumero(), c.getNome(), c.getPartido().getSigla(),
                        c.getBandeira() == null ? "" : "– " + c.getBandeira()));
            }
            sb.append('\n');
        }
        sb.append("Voto de legenda (só vereador): digite os 2 números do partido e CONFIRMA.\n");
        urna.getEleicao().getPartidos().forEach(p ->
                sb.append(String.format("  %d  %-5s %s (%s)%n", p.getNumero(), p.getSigla(), p.getNome(), p.getTema())));
        JTextArea area = new JTextArea(sb.toString(), 26, 90);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Lista de candidatos", JOptionPane.PLAIN_MESSAGE);
    }
}

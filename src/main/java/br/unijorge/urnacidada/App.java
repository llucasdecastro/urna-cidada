package br.unijorge.urnacidada;

import br.unijorge.urnacidada.infra.Configuracao;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.repositorio.VotoRepositorio;
import br.unijorge.urnacidada.servico.DadosFicticios;
import br.unijorge.urnacidada.servico.Urna;
import br.unijorge.urnacidada.ui.JanelaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

/**
 * Ponto de entrada do simulador.
 * Uso: java -jar urna-cidada.jar [caminho/para/config.properties]
 */
public class App {

    public static void main(String[] args) {
        Path arquivoConfig = Path.of(args.length > 0 ? args[0] : "config.properties");
        Configuracao config = new Configuracao(arquivoConfig);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignorado) { /* usa o padrão */ }

            VotoRepositorio repositorio;
            try {
                repositorio = config.criarRepositorio();
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null,
                        "Configuração do Firebase incompleta: " + e.getMessage()
                                + "\nVeja o README. Os votos serão gravados em arquivo local.",
                        "Urna Cidadã", JOptionPane.WARNING_MESSAGE);
                repositorio = new Configuracao(null).criarRepositorio();
            }

            Eleicao eleicao = DadosFicticios.criarEleicao(config.getEleicaoId());
            Urna urna = new Urna(eleicao, repositorio, config.getUrnaId());
            new JanelaPrincipal(urna, config.getPinMesario()).setVisible(true);
        });
    }
}

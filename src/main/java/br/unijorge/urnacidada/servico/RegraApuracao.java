package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.Eleicao;

/**
 * Estratégia para definir quem foi eleito (padrão Strategy).
 * Cada sistema eleitoral tem a sua implementação.
 */
public interface RegraApuracao {

    void definirEleitos(ResultadoCargo resultado, Eleicao eleicao);

    static RegraApuracao para(Cargo cargo) {
        return switch (cargo.getSistema()) {
            case MAJORITARIO -> new RegraMajoritaria();
            case PROPORCIONAL -> new RegraProporcional();
        };
    }
}

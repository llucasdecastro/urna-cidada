package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Candidato;
import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.Partido;

/**
 * O que a tela da urna deve mostrar a partir dos dígitos já digitados.
 */
public record Previsualizacao(Cargo cargo, String digitos, Situacao situacao,
                              Candidato candidato, Partido partido) {

    public enum Situacao {
        /** Nenhum dígito digitado ainda. */
        VAZIO,
        /** Digitando, ainda sem informação suficiente. */
        DIGITANDO,
        /** Número completo de um candidato existente. */
        CANDIDATO,
        /** Só o partido (2 dígitos em cargo proporcional). Confirmar = voto de legenda. */
        PARTIDO,
        /** Número completo inexistente, mas partido válido: voto de legenda. */
        CANDIDATO_INEXISTENTE,
        /** Número que não corresponde a candidato nem a partido. */
        NULO,
        /** Tecla BRANCO pressionada. */
        BRANCO
    }

    public boolean podeConfirmar() {
        return switch (situacao) {
            case CANDIDATO, PARTIDO, CANDIDATO_INEXISTENTE, NULO, BRANCO -> true;
            case VAZIO, DIGITANDO -> false;
        };
    }
}

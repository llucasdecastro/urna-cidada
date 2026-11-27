package br.unijorge.urnacidada.modelo;

/**
 * Abstração de um cargo em disputa.
 * Cada tipo de cargo (proporcional ou majoritário) define suas próprias regras
 * de digitação e de apuração. É aqui que entram abstração e polimorfismo:
 * a urna e a apuração trabalham com {@code Cargo} sem saber qual é a subclasse.
 */
public abstract class Cargo {

    private final String nome;
    private final int quantidadeDigitos;

    protected Cargo(String nome, int quantidadeDigitos) {
        this.nome = nome;
        this.quantidadeDigitos = quantidadeDigitos;
    }

    public String getNome() { return nome; }

    public int getQuantidadeDigitos() { return quantidadeDigitos; }

    /** Indica se o cargo aceita voto apenas no partido (voto de legenda). */
    public abstract boolean aceitaVotoDeLegenda();

    /** Sistema eleitoral usado para definir os eleitos. */
    public abstract SistemaEleitoral getSistema();

    /** Texto educativo exibido na apuração explicando a regra. */
    public abstract String explicacaoDaRegra();

    /** Chave usada para persistência (ex.: "VEREADOR"). */
    public String getChave() {
        return nome.toUpperCase()
                .replace('Ç', 'C').replace('Ã', 'A').replace('É', 'E')
                .replace(' ', '_');
    }

    @Override
    public String toString() { return nome; }
}

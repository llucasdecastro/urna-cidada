package br.unijorge.urnacidada.modelo;

public enum SistemaEleitoral {
    PROPORCIONAL("Proporcional"),
    MAJORITARIO("Majoritário");

    private final String descricao;

    SistemaEleitoral(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}

package br.unijorge.urnacidada.modelo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Voto em um cargo. Classe abstrata com quatro especializações
 * ({@link VotoNominal}, {@link VotoLegenda}, {@link VotoBranco}, {@link VotoNulo}).
 *
 * <p>Como na urna real, o voto NÃO guarda nenhuma informação sobre quem votou:
 * é isso que garante o sigilo. Cada cargo gera um registro separado.</p>
 */
public abstract class Voto {

    private final Cargo cargo;
    private final Instant registradoEm;
    private final String id = UUID.randomUUID().toString();

    protected Voto(Cargo cargo) {
        this.cargo = Objects.requireNonNull(cargo);
        this.registradoEm = Instant.now();
    }

    public Cargo getCargo() { return cargo; }
    public Instant getRegistradoEm() { return registradoEm; }

    public abstract TipoVoto getTipo();

    /** Número que será gravado (vazio para branco). */
    public abstract String getNumero();

    /** Sigla do partido beneficiado, ou null. */
    public String getSiglaPartido() { return null; }

    /** Votos válidos = nominais + legenda. Branco e nulo não são válidos. */
    public boolean isValido() { return getTipo().isValido(); }

    /** Converte o voto em um registro simples para gravar no banco. */
    public RegistroVoto paraRegistro(String urnaId) {
        return new RegistroVoto(cargo.getChave(), getTipo(), getNumero(), getSiglaPartido(),
                registradoEm, urnaId, id);
    }

    @Override
    public String toString() { return getTipo().getDescricao() + " para " + cargo.getNome(); }
}

package br.unijorge.urnacidada.servico;

import br.unijorge.urnacidada.modelo.Cargo;
import br.unijorge.urnacidada.modelo.Eleicao;
import br.unijorge.urnacidada.modelo.Partido;
import br.unijorge.urnacidada.modelo.Voto;
import br.unijorge.urnacidada.modelo.VotoBranco;
import br.unijorge.urnacidada.modelo.VotoLegenda;
import br.unijorge.urnacidada.modelo.VotoNominal;
import br.unijorge.urnacidada.modelo.VotoNulo;
import br.unijorge.urnacidada.repositorio.VotoRepositorio;
import br.unijorge.urnacidada.modelo.Candidato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Regra de negócio da urna: controla a votação sequencial dos cargos de um eleitor.
 * Não sabe nada de interface gráfica nem de banco de dados (depende só da
 * interface {@link VotoRepositorio}), o que permite testá-la isoladamente.
 */
public class Urna {

    public enum Estado { BLOQUEADA, VOTANDO, FIM }

    private final Eleicao eleicao;
    private final VotoRepositorio repositorio;
    private final String urnaId;

    private Estado estado = Estado.BLOQUEADA;
    private int indiceCargo;
    private final StringBuilder digitos = new StringBuilder();
    private boolean branco;
    private final List<Voto> votosDoEleitor = new ArrayList<>();
    private int eleitoresQueVotaram;

    public Urna(Eleicao eleicao, VotoRepositorio repositorio, String urnaId) {
        this.eleicao = Objects.requireNonNull(eleicao);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.urnaId = urnaId;
        if (eleicao.getCargos().isEmpty()) throw new IllegalArgumentException("Eleição sem cargos");
    }

    /** O mesário libera a urna para o próximo eleitor. */
    public void liberar() {
        if (estado == Estado.VOTANDO) throw new IllegalStateException("Já há um eleitor votando");
        estado = Estado.VOTANDO;
        indiceCargo = 0;
        votosDoEleitor.clear();
        limparDigitacao();
    }

    /** Volta para a tela de espera após o "FIM". */
    public void encerrarEleitor() {
        if (estado == Estado.FIM) estado = Estado.BLOQUEADA;
    }

    public boolean digitar(char d) {
        if (estado != Estado.VOTANDO || branco || !Character.isDigit(d)) return false;
        if (digitos.length() >= getCargoAtual().getQuantidadeDigitos()) return false;
        digitos.append(d);
        return true;
    }

    public void corrigir() {
        if (estado == Estado.VOTANDO) limparDigitacao();
    }

    /** BRANCO só funciona se nenhum número foi digitado (igual à urna real). */
    public boolean votarEmBranco() {
        if (estado != Estado.VOTANDO || digitos.length() > 0) return false;
        branco = true;
        return true;
    }

    public Previsualizacao previsualizar() {
        Cargo cargo = getCargoAtual();
        String d = digitos.toString();
        if (branco) return new Previsualizacao(cargo, d, Previsualizacao.Situacao.BRANCO, null, null);
        if (d.isEmpty()) return new Previsualizacao(cargo, d, Previsualizacao.Situacao.VAZIO, null, null);
        if (d.length() < 2) return new Previsualizacao(cargo, d, Previsualizacao.Situacao.DIGITANDO, null, null);

        Partido partido = eleicao.buscarPartido(d).orElse(null);
        boolean completo = d.length() == cargo.getQuantidadeDigitos();

        if (!completo) {
            // Só chega aqui em cargo proporcional (majoritário tem 2 dígitos)
            boolean mostraPartido = partido != null && cargo.aceitaVotoDeLegenda();
            return new Previsualizacao(cargo, d,
                    mostraPartido ? Previsualizacao.Situacao.PARTIDO : Previsualizacao.Situacao.DIGITANDO,
                    null, partido);
        }

        Optional<Candidato> candidato = eleicao.buscarCandidato(cargo, d);
        if (candidato.isPresent()) {
            return new Previsualizacao(cargo, d, Previsualizacao.Situacao.CANDIDATO, candidato.get(), candidato.get().getPartido());
        }
        if (partido != null && cargo.aceitaVotoDeLegenda()) {
            return new Previsualizacao(cargo, d, Previsualizacao.Situacao.CANDIDATO_INEXISTENTE, null, partido);
        }
        return new Previsualizacao(cargo, d, Previsualizacao.Situacao.NULO, null, null);
    }

    /**
     * Confirma o voto do cargo atual. Retorna o voto registrado, ou vazio se ainda
     * não é possível confirmar. Ao confirmar o último cargo, grava todos os votos
     * do eleitor e a urna vai para o estado FIM.
     */
    public Optional<Voto> confirmar() {
        if (estado != Estado.VOTANDO) return Optional.empty();
        Previsualizacao p = previsualizar();
        if (!p.podeConfirmar()) return Optional.empty();

        Voto voto = switch (p.situacao()) {
            case CANDIDATO -> new VotoNominal(p.candidato());
            case PARTIDO, CANDIDATO_INEXISTENTE -> new VotoLegenda(p.cargo(), p.partido());
            case BRANCO -> new VotoBranco(p.cargo());
            case NULO -> new VotoNulo(p.cargo(), p.digitos());
            default -> throw new IllegalStateException("Situação inesperada: " + p.situacao());
        };
        votosDoEleitor.add(voto);

        if (indiceCargo < eleicao.getCargos().size() - 1) {
            indiceCargo++;
            limparDigitacao();
        } else {
            gravarVotosDoEleitor();
            eleitoresQueVotaram++;
            estado = Estado.FIM;
        }
        return Optional.of(voto);
    }

    private void gravarVotosDoEleitor() {
        // Embaralha a ordem antes de gravar: dificulta associar votos de um mesmo eleitor
        List<Voto> copia = new ArrayList<>(votosDoEleitor);
        Collections.shuffle(copia);
        for (Voto v : copia) {
            repositorio.salvar(v.paraRegistro(urnaId));
        }
        votosDoEleitor.clear();
    }

    private void limparDigitacao() {
        digitos.setLength(0);
        branco = false;
    }

    public Cargo getCargoAtual() { return eleicao.getCargos().get(indiceCargo); }
    public Estado getEstado() { return estado; }
    public Eleicao getEleicao() { return eleicao; }
    public VotoRepositorio getRepositorio() { return repositorio; }
    public String getUrnaId() { return urnaId; }
    public int getEleitoresQueVotaram() { return eleitoresQueVotaram; }
    public int getPosicaoCargo() { return indiceCargo + 1; }
    public int getTotalCargos() { return eleicao.getCargos().size(); }
}

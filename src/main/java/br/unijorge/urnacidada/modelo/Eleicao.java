package br.unijorge.urnacidada.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agrega os cargos (na ordem de votação), os partidos e os candidatos de uma eleição.
 */
public class Eleicao {

    private final String id;
    private final String titulo;
    private final String municipio;
    private final List<Cargo> cargos = new ArrayList<>();
    private final Map<Integer, Partido> partidos = new LinkedHashMap<>();
    private final List<Candidato> candidatos = new ArrayList<>();

    public Eleicao(String id, String titulo, String municipio) {
        this.id = id;
        this.titulo = titulo;
        this.municipio = municipio;
    }

    public void adicionarCargo(Cargo cargo) { cargos.add(cargo); }

    public void adicionarPartido(Partido partido) {
        if (partidos.putIfAbsent(partido.getNumero(), partido) != null) {
            throw new IllegalArgumentException("Partido já cadastrado: " + partido.getNumero());
        }
    }

    public void adicionarCandidato(Candidato candidato) {
        if (!cargos.contains(candidato.getCargo())) {
            throw new IllegalArgumentException("Cargo não pertence à eleição: " + candidato.getCargo());
        }
        if (buscarCandidato(candidato.getCargo(), candidato.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Número já usado para " + candidato.getCargo() + ": " + candidato.getNumero());
        }
        candidatos.add(candidato);
    }

    public Optional<Candidato> buscarCandidato(Cargo cargo, String numero) {
        return candidatos.stream()
                .filter(c -> c.getCargo().equals(cargo) && c.getNumero().equals(numero))
                .findFirst();
    }

    public Optional<Partido> buscarPartido(String numero) {
        if (numero == null || numero.length() < 2) return Optional.empty();
        try {
            return Optional.ofNullable(partidos.get(Integer.parseInt(numero.substring(0, 2))));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Partido> buscarPartidoPorSigla(String sigla) {
        return partidos.values().stream().filter(p -> p.getSigla().equals(sigla)).findFirst();
    }

    public Optional<Cargo> buscarCargo(String chave) {
        return cargos.stream().filter(c -> c.getChave().equals(chave)).findFirst();
    }

    public List<Candidato> candidatosDo(Cargo cargo) {
        return candidatos.stream().filter(c -> c.getCargo().equals(cargo)).toList();
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMunicipio() { return municipio; }
    public List<Cargo> getCargos() { return Collections.unmodifiableList(cargos); }
    public List<Partido> getPartidos() { return List.copyOf(partidos.values()); }
    public List<Candidato> getCandidatos() { return Collections.unmodifiableList(candidatos); }
}

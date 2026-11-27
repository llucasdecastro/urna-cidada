package br.unijorge.urnacidada.repositorio;

import br.unijorge.urnacidada.modelo.RegistroVoto;

import java.util.List;

/**
 * Contrato de persistência dos votos. A urna depende desta abstração e não de
 * uma implementação concreta (Firestore, arquivo ou memória).
 *
 * <p>Não existe método para alterar ou apagar um voto: depois de gravado,
 * o registro é imutável, como na urna real.</p>
 */
public interface VotoRepositorio {

    void salvar(RegistroVoto registro) throws RepositorioException;

    List<RegistroVoto> listarTodos() throws RepositorioException;

    /** Nome amigável exibido na interface (ex.: "Firebase Cloud Firestore"). */
    String getDescricao();
}

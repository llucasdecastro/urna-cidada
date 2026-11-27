package br.unijorge.urnacidada.modelo;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de voto como ele é gravado no banco (equivalente simplificado ao
 * Registro Digital do Voto – RDV). Objeto imutável, sem nenhum dado do eleitor.
 */
public record RegistroVoto(String cargo, TipoVoto tipo, String numero, String partido,
                           Instant registradoEm, String urnaId, String id) {

    public RegistroVoto(String cargo, TipoVoto tipo, String numero, String partido,
                        Instant registradoEm, String urnaId) {
        this(cargo, tipo, numero, partido, registradoEm, urnaId, UUID.randomUUID().toString());
    }
}

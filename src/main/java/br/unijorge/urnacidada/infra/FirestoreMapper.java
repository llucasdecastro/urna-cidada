package br.unijorge.urnacidada.infra;

import br.unijorge.urnacidada.modelo.RegistroVoto;
import br.unijorge.urnacidada.modelo.TipoVoto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte {@link RegistroVoto} de/para o formato de documento do Firestore
 * (cada campo é um objeto tipado, ex.: {"stringValue": "VEREADOR"}).
 */
public final class FirestoreMapper {

    private FirestoreMapper() { }

    public static Map<String, Object> paraDocumento(RegistroVoto r) {
        Map<String, Object> campos = new LinkedHashMap<>();
        campos.put("cargo", Map.of("stringValue", r.cargo()));
        campos.put("tipo", Map.of("stringValue", r.tipo().name()));
        campos.put("numero", Map.of("stringValue", r.numero() == null ? "" : r.numero()));
        campos.put("partido", r.partido() == null ? Map.of("nullValue", "NULL_VALUE") : Map.of("stringValue", r.partido()));
        campos.put("registradoEm", Map.of("timestampValue", r.registradoEm().toString()));
        campos.put("urnaId", Map.of("stringValue", r.urnaId() == null ? "" : r.urnaId()));
        return Map.of("fields", campos);
    }

    @SuppressWarnings("unchecked")
    public static RegistroVoto deDocumento(Map<String, Object> documento) {
        Map<String, Object> campos = (Map<String, Object>) documento.get("fields");
        if (campos == null) throw new IllegalArgumentException("Documento sem campos");
        return new RegistroVoto(
                texto(campos, "cargo"),
                tipo(texto(campos, "tipo")),
                texto(campos, "numero"),
                texto(campos, "partido"),
                instante(campos.get("registradoEm")),
                texto(campos, "urnaId"));
    }

    /** Formato "achatado" usado no arquivo de contingência. */
    public static Map<String, Object> paraMapaSimples(RegistroVoto r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cargo", r.cargo());
        m.put("tipo", r.tipo().name());
        m.put("numero", r.numero());
        m.put("partido", r.partido());
        m.put("registradoEm", r.registradoEm().toString());
        m.put("urnaId", r.urnaId());
        return m;
    }

    public static RegistroVoto deMapaSimples(Map<String, Object> m) {
        Object quando = m.get("registradoEm");
        return new RegistroVoto((String) m.get("cargo"), tipo((String) m.get("tipo")), (String) m.get("numero"),
                (String) m.get("partido"), quando == null ? Instant.EPOCH : Instant.parse((String) quando),
                (String) m.get("urnaId"));
    }

    @SuppressWarnings("unchecked")
    private static String texto(Map<String, Object> campos, String nome) {
        Object campo = campos.get(nome);
        if (!(campo instanceof Map)) return null;
        Object v = ((Map<String, Object>) campo).get("stringValue");
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    private static Instant instante(Object campo) {
        if (campo instanceof Map) {
            Object v = ((Map<String, Object>) campo).get("timestampValue");
            if (v != null) return Instant.parse(v.toString());
        }
        return Instant.EPOCH;
    }

    private static TipoVoto tipo(String nome) {
        if (nome == null) return null;
        try {
            return TipoVoto.valueOf(nome);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

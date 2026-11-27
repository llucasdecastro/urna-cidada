package br.unijorge.urnacidada.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitor/escritor de JSON mínimo, suficiente para conversar com a API REST
 * do Firestore sem precisar de bibliotecas externas.
 * Objetos viram {@code Map<String,Object>}, arrays viram {@code List<Object>}.
 */
public final class Json {

    private Json() { }

    // ------------------------------------------------------------- escrita
    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb);
        return sb.toString();
    }

    private static void escrever(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            texto(s, sb);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                texto(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escrever(e.getValue(), sb);
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> it) {
            sb.append('[');
            boolean primeiro = true;
            for (Object o : it) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(o, sb);
            }
            sb.append(']');
        } else {
            texto(v.toString(), sb);
        }
    }

    private static void texto(String s, StringBuilder sb) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ------------------------------------------------------------- leitura
    public static Object ler(String json) {
        Leitor l = new Leitor(json);
        Object v = l.valor();
        l.espacos();
        if (l.pos != json.length()) throw l.erro("conteúdo extra após o JSON");
        return v;
    }

    private static final class Leitor {
        private final String s;
        private int pos;

        Leitor(String s) { this.s = s; }

        Object valor() {
            espacos();
            if (pos >= s.length()) throw erro("fim inesperado");
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> objeto();
                case '[' -> lista();
                case '"' -> texto();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> numero();
            };
        }

        Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            pos++; espacos();
            if (s.charAt(pos) == '}') { pos++; return m; }
            while (true) {
                espacos();
                String chave = texto();
                espacos();
                esperar(':');
                m.put(chave, valor());
                espacos();
                char c = s.charAt(pos++);
                if (c == '}') return m;
                if (c != ',') throw erro("esperado ',' ou '}'");
            }
        }

        List<Object> lista() {
            List<Object> l = new ArrayList<>();
            pos++; espacos();
            if (s.charAt(pos) == ']') { pos++; return l; }
            while (true) {
                l.add(valor());
                espacos();
                char c = s.charAt(pos++);
                if (c == ']') return l;
                if (c != ',') throw erro("esperado ',' ou ']'");
            }
        }

        String texto() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) throw erro("texto não terminado");
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"', '\\', '/' -> sb.append(e);
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> { sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16)); pos += 4; }
                        default -> throw erro("escape inválido");
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        Object numero() {
            int ini = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String n = s.substring(ini, pos);
            if (n.isEmpty()) throw erro("valor inválido");
            if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
            return Long.parseLong(n);
        }

        Object literal(String palavra, Object valor) {
            if (!s.startsWith(palavra, pos)) throw erro("esperado " + palavra);
            pos += palavra.length();
            return valor;
        }

        void esperar(char c) {
            if (pos >= s.length() || s.charAt(pos) != c) throw erro("esperado '" + c + "'");
            pos++;
        }

        void espacos() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        IllegalArgumentException erro(String msg) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + msg);
        }
    }
}

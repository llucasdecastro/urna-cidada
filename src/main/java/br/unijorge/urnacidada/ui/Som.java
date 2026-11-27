package br.unijorge.urnacidada.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;

/** Gera os "bipes" da urna sinteticamente (sem arquivos de áudio). */
public final class Som {

    private Som() { }

    public static void tecla() { tocar(1000, 60); }

    public static void confirma() { tocar(1100, 120); }

    /** O famoso som de fim de votação: uma sequência de bipes. */
    public static void fim() {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                tocarAgora(1050, 90);
                dormir(25);
            }
            tocarAgora(1050, 450);
        }, "som-fim").start();
    }

    private static void tocar(int hz, int ms) {
        new Thread(() -> tocarAgora(hz, ms), "som").start();
    }

    private static void tocarAgora(int hz, int ms) {
        float taxa = 44100f;
        try (SourceDataLine linha = AudioSystem.getSourceDataLine(new AudioFormat(taxa, 8, 1, true, false))) {
            linha.open();
            linha.start();
            byte[] buf = new byte[(int) (taxa * ms / 1000)];
            for (int i = 0; i < buf.length; i++) {
                double onda = Math.sin(2 * Math.PI * i * hz / taxa) >= 0 ? 1 : -1; // onda quadrada, som de "bip"
                buf[i] = (byte) (onda * 40);
            }
            linha.write(buf, 0, buf.length);
            linha.drain();
        } catch (Exception | Error e) {
            try { Toolkit.getDefaultToolkit().beep(); } catch (Exception | Error ignorado) { /* sem áudio */ }
        }
    }

    private static void dormir(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

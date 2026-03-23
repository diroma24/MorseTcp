import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;

public class MorseTranslator implements AutoCloseable {
    private static final int SAMPLE_RATE = 44100;
    private static final int FREQUENCY = 550;
    private static final int DOT_DURATION = 120;
    private static final int DASH_DURATION = DOT_DURATION * 3;

    private static final Map<Character, String> morseMap = new HashMap<>();

    static {
        //Letters
        morseMap.put('A', ".-");
        morseMap.put('B', "-...");
        morseMap.put('C', "-.-.");
        morseMap.put('D', "-..");
        morseMap.put('E', ".");
        morseMap.put('F', "..-.");
        morseMap.put('G', "--.");
        morseMap.put('H', "....");
        morseMap.put('I', "..");
        morseMap.put('J', ".---");
        morseMap.put('K', "-.-");
        morseMap.put('L', ".-..");
        morseMap.put('M', "--");
        morseMap.put('N', "-.");
        morseMap.put('O', "---");
        morseMap.put('P', ".--.");
        morseMap.put('Q', "--.-");
        morseMap.put('R', ".-.");
        morseMap.put('S', "...");
        morseMap.put('T', "-");
        morseMap.put('U', "..-");
        morseMap.put('V', "...-");
        morseMap.put('W', ".--");
        morseMap.put('X', "-..-");
        morseMap.put('Y', "-.--");
        morseMap.put('Z', "--..");

        // Numbers
        morseMap.put('0', "-----");
        morseMap.put('1', ".----");
        morseMap.put('2', "..---");
        morseMap.put('3', "...--");
        morseMap.put('4', "....-");
        morseMap.put('5', ".....");
        morseMap.put('6', "-....");
        morseMap.put('7', "--...");
        morseMap.put('8', "---..");
        morseMap.put('9', "----.");

        // Blank
        morseMap.put(' ', "/");
    }

    public String translateToMorse(String text) {
        if (text == null) return "";

        StringBuilder result = new StringBuilder();
        String upperText = text.toUpperCase();

        for (char c : upperText.toCharArray()) {
            if (morseMap.containsKey(c)) {
                result.append(morseMap.get(c)).append(" ");
            }
        }

        return result.toString().trim();
    }

    public void playMorse(String morseCode) {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
            sdl.open(af);
            sdl.start();

            for (char c : morseCode.toCharArray()) {
                switch (c) {
                    case '.': playTone(sdl, DOT_DURATION); break;
                    case '-': playTone(sdl, DASH_DURATION); break;
                    case ' ': Thread.sleep(DOT_DURATION * 2); break;
                    case '/': Thread.sleep(DOT_DURATION * 6); break;
                }
                Thread.sleep(DOT_DURATION); // Espacio entre símbolos
            }
            sdl.drain();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void playTone(SourceDataLine sdl, int duration) {
        int length = (int) (SAMPLE_RATE * duration / 1000.0);
        byte[] buffer = new byte[length * 2];

        int envelopeSamples = (int) (SAMPLE_RATE * 0.005);

        for (int i = 0; i < length; i++) {
            double angle = 2.0 * Math.PI * i * FREQUENCY / SAMPLE_RATE;
            double lfo = Math.sin(angle);

            double volume = 1.0;
            if (i < envelopeSamples) {
                volume = (double) i / envelopeSamples;
            }
            else if (i > length - envelopeSamples) {
                volume = (double) (length - i) / envelopeSamples;
            }

            short amplitude = (short) (lfo * Short.MAX_VALUE * volume * 0.8);
            buffer[2 * i] = (byte) (amplitude & 0xFF);
            buffer[2 * i + 1] = (byte) ((amplitude >> 8) & 0xFF);
        }
        sdl.write(buffer, 0, buffer.length);
    }

    @Override
    public void close() throws Exception {

    }
}
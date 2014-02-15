package bs.bsonify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Application entry
 */
public class Bsonify {

    public static void main(String[] args) throws IOException {

        final InputStream in;
        String filename = filename(args);
        if (filename != null) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        ColorScheme color = colorScheme(args);

        MainReader.formatStream(new OutputStreamWriter(System.out), new InputStreamReader(in), color);

        in.close();
    }

    private static String filename(String[] args) {
        for (String arg : args) {
            if (!arg.contains("-")) {
                return arg;
            }
        }

        return null;
    }

    private static ColorScheme colorScheme(String[] args) {
        ColorScheme color = ColorScheme.LIGHT;
        for (String arg : args) {
            if (arg.equals("-mono")) {
                color = ColorScheme.MONO;
                break;
            } else if (arg.equals("-dark")) {
                color = ColorScheme.DARK;
                break;
            }

        }
        return color;
    }

}

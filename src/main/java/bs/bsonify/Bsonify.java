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

        Options options = parseArgs(args);

        final InputStream in;
        if (options.hasFilename()) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        MainReader.formatStream(new OutputStreamWriter(System.out), new InputStreamReader(in), options);

        in.close();
    }

    private static Options parseArgs(String[] args) {
        Options options = new Options();
        for (String arg : args) {
            if (arg.equals("-mono")) {
                options.setColor(ColorScheme.MONO);
            } else if (arg.equals("-dark")) {
                options.setColor(ColorScheme.DARK);
            } else if (arg.equals("-compact")) {
                options.setCompact(true);
            } else if (!arg.contains("-") && !options.hasFilename()) {
                options.setFilename(arg);
            } else {
                throw new RuntimeException("Unknown option: " + arg);
            }
        }
        return options;
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

package bs.bsonify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Application entry
 */
public class Bsonify {

    public static void main(String[] args) throws IOException {

        if (helpOrVersion(args)) {
            return;
        }

        final Options options;
        try {
            options = parseArgs(args);
        } catch (InvalidOptionException e) {
            printInvalidOption(e);
            return;
        }

        final InputStream in;
        if (options.hasFilename()) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        InputStreamReader isw = new InputStreamReader(in);
        MainReader.formatStream(osw, isw, options);

        osw.close();
        in.close();
    }

    private static Options parseArgs(String[] args) throws InvalidOptionException {
        Options options = new Options();
        for (String arg : args) {
            if (arg.equals("-mono")) {
                options.setColor(ColorScheme.MONO);
            } else if (arg.equals("-dark")) {
                options.setColor(ColorScheme.DARK);
            } else if (arg.equals("-light")) {
                options.setColor(ColorScheme.LIGHT);
            } else if (arg.equals("-compact")) {
                options.setCompact(true);
            } else if (arg.equals("-jsononly")) {
                options.setJsononly(true);
            } else if (!arg.contains("-") && !options.hasFilename()) {
                options.setFilename(arg);
            } else {
                throw new InvalidOptionException("bsonify: invalid option: '" + arg + "'");
            }
        }
        return options;
    }

    private static boolean helpOrVersion(String[] args) {
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("--h")) {
                printResource("/help.txt");
                return true;
            } else if (arg.equals("--version") || arg.equals("--v")) {
                printResource("/version.txt");
                return true;
            }

        }

        return false;
    }

    private static void printResource(String resname) {
        InputStream in = Bsonify.class.getResourceAsStream(resname);
        copy(in, System.out);
    }

    private static void printInvalidOption(InvalidOptionException e) {
        System.out.println(e.getMessage());
        System.out.println("Try 'java -jar bsonify.jar --help' for more information.");
    }

    private static void copy(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[100];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

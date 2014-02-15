package bs.bsonify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

public class MainReader {
    private static final char OPEN_ARRAY = '[';
    private static final char OPEN_OBJECT = '{';

    private static final int READ_AHEAD_LIMIT = 100000;

    public static void formatStream(Writer writer, Reader reader, ColorScheme color) throws IOException {
        Reader isr = new BufferedReader(reader);

        char[] buffer = new char[512];
        int bytesRead;
        isr.mark(READ_AHEAD_LIMIT);
        while ((bytesRead = isr.read(buffer)) != -1) {

            int jsonIndex = findJsonStart(buffer, bytesRead);
            boolean possiblyContainsJson = jsonIndex > -1;
            if (possiblyContainsJson) {

                // first print non-json part
                writer.write(Arrays.copyOf(buffer, jsonIndex));

                isr.reset();
                isr.skip(jsonIndex);
                isr.mark(READ_AHEAD_LIMIT);

                long numRead = JsonFormatter.printJson(writer, isr, color);

                isr.reset();

                if (numRead > 0) {

                    // JSON has been parsed and printed, skip the JSON characters
                    isr.skip(numRead);

                } else {

                    // malformed or no JSON, print one char to not interpret it as JSON content in the next round
                    writer.write(buffer[jsonIndex]);
                    isr.skip(1);

                }

            } else {

                print(writer, buffer, bytesRead);

            }
            
            writer.flush();
            
            isr.mark(READ_AHEAD_LIMIT);
        }

    }

    private static void print(Writer w, char[] buffer, int bytesRead) throws IOException {
        w.write(buffer, 0, bytesRead);
    }

    /**
     * Returns the index of the first character possibly indicating a json object or array.
     * 
     * @param buffer the characters to search
     * @param len end of the array to search
     * @return the index of the first possible json object or array, -1 if not found
     */
    private static int findJsonStart(char[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            if (buffer[i] == OPEN_ARRAY || buffer[i] == OPEN_OBJECT) {
                return i;
            }
        }
        return -1;
    }
}

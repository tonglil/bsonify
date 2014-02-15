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
        if (args.length == 1) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        
        MainReader.formatStream(new OutputStreamWriter(System.out), new InputStreamReader(in));

        in.close();
    }

}

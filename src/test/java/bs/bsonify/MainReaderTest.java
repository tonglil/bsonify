package bs.bsonify;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class MainReaderTest {
    @Test
    public void testFormatStreamPlainText() throws IOException {
        String noJsonStart = "asdf1234~`!@#$%^&*(\"):;\"']}=+-\n_,.<>/?";
        Reader in = new StringReader(noJsonStart);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, ColorScheme.DARK);

        Assert.assertEquals(noJsonStart, out.toString());
    }

    // TODO testFormatStreamPlainJsonArray and others, after monochrome feature

    /**
     * Simulates a tail where one thread of execution writes to the character stream, and another thread executes reads it and
     * runs the Bsonify filter.
     */
    @Test @Ignore
    public void testFormatStreamStreaming() throws IOException {

        PipedOutputStream sender = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(sender);
        Reader isr = new InputStreamReader(pis);

        // for the writer thread 
        final OutputStreamWriter senderWriter = new OutputStreamWriter(sender);

        // for the output of the reader thread 
        final StringWriter sw = new StringWriter();

        Runnable writer = new Runnable() {
            public void run() {
                sleep(100);
                write(senderWriter, "1234{\"grmbls\": [\"a\"");
                sleep(100);
                write(senderWriter, ",\n");
                sleep(100);
                write(senderWriter, "\"b\"]}asdf");
            }
        };
        new Thread(writer).start();

        try {
            
            // exercise
            MainReader.formatStream(sw, isr, ColorScheme.MONO);
            
        } catch (IOException e) {
            
            // expected exception, broken pipe because the writer is dead
            
            // assert TODO
            System.out.println(sw.toString());
            Assert.fail("assert NYI");
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(Writer w, String s) {
        try {
            w.write(s);
            w.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

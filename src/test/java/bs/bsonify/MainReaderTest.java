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
        MainReader.formatStream(out, in);

        Assert.assertEquals(noJsonStart, out.toString());
    }

    // TODO testFormatStreamPlainJsonArray and others, after monochrome feature

    @Test @Ignore
    public void testFormatStreamStreaming() throws IOException {

        PipedOutputStream sender = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(sender);
        Reader isr = new InputStreamReader(pis);

        final StringWriter sw = new StringWriter();

        final OutputStreamWriter senderWriter = new OutputStreamWriter(sender);

        Runnable writer = new Runnable() {
            public void run() {
                sleep(1000);
                System.out.println("<W>");
                write(senderWriter, "1234[\"a\"");
                sleep(1000);
                System.out.println("<W>");
                write(senderWriter, ",");
                sleep(1000);
                System.out.println("<W>");
                write(senderWriter, "\"b\"]987");
                sleep(1000);  // keep write end of pipe open for a while to give the reader some time
            }
        };
        new Thread(writer).start();

        Runnable reader = new Runnable() {
            private String s = "";

            public void run() {
                while (s.length() < 100) {
                    sleep(100);
                    if (sw.getBuffer().length() > s.length()) {
                        s = sw.toString();
//                        System.out.println("<R>" + s);
                    }
                }
            }
        };
        new Thread(reader).start();

        // exercise
        try {
        MainReader.formatStream(sw, isr);
        }catch(IOException e) {
            System.out.println(sw.toString());
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

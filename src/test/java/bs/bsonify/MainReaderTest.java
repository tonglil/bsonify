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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class MainReaderTest {
    
    @Test
    public void testFormatPlainText() throws IOException {
        String noJsonStart = "asdf1234~`!@#$%^&*(\"):;\"']}=+-\n_,.<>/?";
        Reader in = new StringReader(noJsonStart);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        Assert.assertEquals(noJsonStart, out.toString());
    }

    @Test
    public void testFormatArray() throws IOException {
        String input = "[\"a\", \"b\"]";
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = "[\"a\", \"b\"]";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testFormatObject() throws IOException {
        String input = "{\"a\":\"b\"}";
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = "{\n    \"a\": \"b\"\n}";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testFormatJsonAndText() throws IOException {
        String input = "asdf{\"a\":\"b\"}1234";
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = "asdf{\n    \"a\": \"b\"\n}1234";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testFormatComplexJson() throws IOException {
        String input = IOUtils.toString(getClass().getResourceAsStream("testFormatComplexJson.input.txt"));
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = IOUtils.toString(getClass().getResourceAsStream("testFormatComplexJson.expected.txt"));
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testFormatJsonOneeTochNiet() throws IOException {
        String input = "[\"a\", asdf";
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = "[\"a\", asdf";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testFormatObjectBetweenStringValues() throws IOException {
        String input = "[\"a\", {\"b\": \"c\"}, \"d\"]";
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = "[\"a\",\n    {\n        \"b\": \"c\"\n    },\n    \"d\"]";
        Assert.assertEquals(expected, out.toString());
    }

    /**
     * This won't happen very often, it needs to render properly
     */
    @Test
    public void testFormatObjectBetweenValues() throws IOException {
        String input = IOUtils.toString(getClass().getResourceAsStream("testFormatObjectBetweenValues.input.txt"));
        Reader in = new StringReader(input);
        Writer out = new StringWriter();
        MainReader.formatStream(out, in, createOptions());

        String expected = IOUtils.toString(getClass().getResourceAsStream("testFormatObjectBetweenValues.expected.txt"));
        String actual = out.toString();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Simulates a tail where one thread of execution writes to the character stream, and another thread executes reads it and
     * runs the Bsonify filter.
     */
    @Test
    public void testFormatStreaming() throws IOException, InterruptedException {

        // prepare...
        PipedOutputStream sender = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(sender);
        final Reader isr = new InputStreamReader(pis);

        // for the writer thread
        final OutputStreamWriter senderWriter = new OutputStreamWriter(sender);

        // for the output of the reader thread
        final StringWriter sw = new StringWriter();

        final CountDownLatch writerDone = new CountDownLatch(1);
        Runnable writer = new Runnable() {
            public void run() {
                sleep(100);
                write(senderWriter, "12\n");
                sleep(100);
                write(senderWriter, "34{\"grmbls\": [\"a\"");
                sleep(100);
                write(senderWriter, ",\n");
                sleep(100);
                write(senderWriter, "\"b\"]}asdf");
                sleep(100);
                writerDone.countDown();
                sleep(1000);
            }
        };

        Runnable reader = new Runnable() {
            public void run() {
                try {

                    // exercise
                    MainReader.formatStream(sw, isr, createOptions());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new Thread(reader).start();
        new Thread(writer).start();

        writerDone.await(1, TimeUnit.SECONDS);

        // assert
        String expected = IOUtils.toString(getClass().getResourceAsStream("testFormatStreaming.expected.txt"));
        Assert.assertEquals(expected, sw.toString());
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

    private static Options createOptions() {
        Options options = new Options();
        options.setColor(ColorScheme.MONO);
        return options;
    }
}

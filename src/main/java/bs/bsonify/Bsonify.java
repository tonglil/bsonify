package bs.bsonify;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.impl.JsonParserBase;

/**
 * Application entry
 */
public class Bsonify {

    private static final char OPEN_ARRAY = '[';
    private static final char OPEN_OBJECT = '{';

    private static final int READ_AHEAD_LIMIT = 100000;

    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {

        final InputStream in;
        if (args.length == 1) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        Reader isr = new BufferedReader(new InputStreamReader(in));

        char[] buffer = new char[512];
        int bytesRead;
        isr.mark(READ_AHEAD_LIMIT);
        while ((bytesRead = isr.read(buffer)) != -1) {
            
            System.out.print("*"+bytesRead+"*");
            
            int jsonIndex = findJsonStart(buffer, bytesRead);
            boolean possiblyContainsJson = jsonIndex > -1;
            if (possiblyContainsJson) {

                // first print non-json part
                System.out.print(Arrays.copyOf(buffer, jsonIndex));

                isr.reset();
                isr.skip(jsonIndex);
                isr.mark(READ_AHEAD_LIMIT);

                int numRead = printJsonJackson(isr);
                
                isr.reset();
                
                if (numRead > 0) {

                    // JSON has been parsed and printed, skip the JSON characters
                    isr.skip(numRead);

                } else {

                    // malformed or no JSON, print one char to not interpret it as JSON content in the next round
                    System.out.print(buffer[jsonIndex]);
                    isr.skip(1);

                }

            } else {

                print(buffer, bytesRead);

            }
            isr.mark(READ_AHEAD_LIMIT);
        }

        in.close();
    }

    private static void print(char[] buffer, int bytesRead) {
        for (int i = 0; i < bytesRead; i++) {
            System.out.print(buffer[i]);
        }
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

    /**
     * 
     */
    private static int printJsonJackson(Reader isr) {
        try {
            StringWriter target = new StringWriter();

            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createJsonParser(isr);
            JsonToken token = jp.nextToken();
            ParsingContext ctx = new ParsingContext(jp, target, token, new JsonModel());

            parseJsonDispatchNewElement(ctx);

            // dirty: peek into the reader to find out the number of JSON characters read from the inputstream
            Field posField = JsonParserBase.class.getDeclaredField("_inputPtr");
            posField.setAccessible(true);
            int numRead = (Integer) posField.get(jp);

            Renderer.resetColor(target);
            System.out.print(target);

            return numRead;

        } catch (JsonParseException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        } catch (NoSuchFieldException e) {
            return 0;
        } catch (SecurityException e) {
            return 0;
        } catch (IllegalArgumentException e) {
            return 0;
        } catch (IllegalAccessException e) {
            return 0;
        } catch (RenderException e) {
            return 0;
        }

    }

    private static void parseJsonDispatchNewElement(ParsingContext ctx) throws JsonParseException, IOException {

        switch (ctx.token()) {
        case START_ARRAY:
            parseJsonArray(ctx);
            break;
        case START_OBJECT:
            parseJsonObject(ctx);
            break;
        case FIELD_NAME:
            parseJsonFieldName(ctx);
            break;
        default:
            parseJsonValue(ctx);
        }
    }

    private static void parseJsonObject(ParsingContext ctx) throws JsonParseException, IOException {

        ctx.model().add(new Element(ElementType.START_OBJECT, null));
        Renderer.render(ctx.model(), ctx.target());

        JsonToken token;
        while ((token = ctx.jp().nextToken()) != JsonToken.END_OBJECT) {
            ctx.setToken(token);
            parseJsonDispatchNewElement(ctx);
        }

        ctx.model().add(new Element(ElementType.END_OBJECT, null));
        Renderer.render(ctx.model(), ctx.target());
    }

    private static void parseJsonArray(ParsingContext ctx) throws JsonParseException, IOException {
        ctx.model().add(new Element(ElementType.START_ARRAY, null));
        Renderer.render(ctx.model(), ctx.target());

        JsonToken token;
        while ((token = ctx.jp().nextToken()) != JsonToken.END_ARRAY) {
            ctx.setToken(token);
            parseJsonDispatchNewElement(ctx);
        }

        ctx.model().add(new Element(ElementType.END_ARRAY, null));
        Renderer.render(ctx.model(), ctx.target());
    }

    private static void parseJsonValue(ParsingContext ctx) throws JsonParseException, IOException {

        final String value;

        switch (ctx.token()) {
        case VALUE_FALSE:
        case VALUE_TRUE:
        case VALUE_NULL:
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
        case VALUE_STRING:
            value = ctx.jp().getText();
            break;
        case NOT_AVAILABLE:
            value = "-- N.A. --";
            break;
        case VALUE_EMBEDDED_OBJECT:
            value = "-- <embedded object> --";
            break;
        default:
            value = "";
            break;
        }

        ctx.model().add(new Element(ElementType.VALUE, value));

        Renderer.render(ctx.model(), ctx.target());
    }

    private static void parseJsonFieldName(ParsingContext ctx) throws JsonParseException, IOException {

        ctx.model().add(new Element(ElementType.FIELD_NAME, ctx.jp().getText()));

        Renderer.render(ctx.model(), ctx.target());
    }
}

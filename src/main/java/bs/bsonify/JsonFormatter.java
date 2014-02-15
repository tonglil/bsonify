package bs.bsonify;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.impl.JsonParserBase;

public final class JsonFormatter {

    public  static long printJson(Writer writer, Reader r) {
        try {
            StringWriter target = new StringWriter();
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createJsonParser(r);
            JsonToken token = jp.nextToken();
            ParsingContext ctx = new ParsingContext(jp, target, token, new JsonModel());

            parseJsonDispatchNewElement(ctx);

            Renderer.resetColor(target);
            writer.write(target.toString());
            
            // peek into the reader to find out the number of 'JSON' characters read from the inputstream
            Field inpPtrField = JsonParserBase.class.getDeclaredField("_tokenInputTotal");
            inpPtrField.setAccessible(true);
            long charsReadBeforeCurrentToken = (Long) inpPtrField.get(jp);
            long jsonCharsRead = charsReadBeforeCurrentToken + 1; // last token is 1 char

            return jsonCharsRead;

        } catch (JsonParseException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        } catch (SecurityException e) {
            return 0;
        } catch (IllegalArgumentException e) {
            return 0;
        } catch (NoSuchFieldException e) {
            return 0;
        } catch (IllegalAccessException e) {
            return 0;
        } catch (RenderException e) {
            return 0;
        } catch (ParsingException e) {
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

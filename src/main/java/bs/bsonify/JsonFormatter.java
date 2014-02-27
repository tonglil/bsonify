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

    public static long printJson(Writer writer, Reader reader, Options options) {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createJsonParser(reader);
            JsonToken token = jp.nextToken();
            Writer target = new StringWriter();
            ParsingContext ctx = new ParsingContext(jp, token, new JsonModel());

            Renderer renderer = new Renderer(target, options);
            parseJsonDispatchNewElement(ctx, renderer);
            

            renderer.resetColor();

            long jsonCharsRead = peekCharsRead(jp);

            writer.write(target.toString());
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

    /**
     * peek into the parser to find out the number of 'JSON' characters read from the inputstream
     */
    private static long peekCharsRead(JsonParser jp) throws NoSuchFieldException, IllegalAccessException {
        Field inpPtrField = JsonParserBase.class.getDeclaredField("_tokenInputTotal");
        inpPtrField.setAccessible(true);
        long charsReadBeforeCurrentToken = (Long) inpPtrField.get(jp);
        long jsonCharsRead = charsReadBeforeCurrentToken + 1; // last token is 1 char
        return jsonCharsRead;
    }

    private static void parseJsonDispatchNewElement(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {

        switch (ctx.token()) {
        case START_ARRAY:
            parseJsonArray(ctx, renderer);
            break;
        case START_OBJECT:
            parseJsonObject(ctx, renderer);
            break;
        case FIELD_NAME:
            parseJsonFieldName(ctx, renderer);
            break;
        case VALUE_FALSE:
        case VALUE_TRUE:
        case VALUE_NULL:
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
            parseJsonNumberTrueFalseOrNullValue(ctx, renderer);
            break;
        case VALUE_STRING:
            parseJsonStringValue(ctx, renderer);
            break;
        default:
            parseJsonNaOrEmbeddedValue(ctx, renderer);
        }
    }

    private static void parseJsonObject(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {

        ctx.model().add(new Element(ElementType.START_OBJECT, null));
        renderer.render(ctx.model());

        JsonToken token;
        while ((token = ctx.jp().nextToken()) != JsonToken.END_OBJECT) {
            ctx.setToken(token);
            parseJsonDispatchNewElement(ctx, renderer);
        }

        ctx.model().add(new Element(ElementType.END_OBJECT, null));
        renderer.render(ctx.model());
    }

    private static void parseJsonArray(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {
        ctx.model().add(new Element(ElementType.START_ARRAY, null));
        renderer.render(ctx.model());

        JsonToken token;
        while ((token = ctx.jp().nextToken()) != JsonToken.END_ARRAY) {
            ctx.setToken(token);
            parseJsonDispatchNewElement(ctx, renderer);
        }

        ctx.model().add(new Element(ElementType.END_ARRAY, null));
        renderer.render(ctx.model());
    }

    private static void parseJsonNumberTrueFalseOrNullValue(ParsingContext ctx, Renderer renderer) throws JsonParseException,
            IOException {

        String value = ctx.jp().getText();
        ctx.model().add(new Element(ElementType.NUMBER_TRUE_FALSE_OR_NULL_VALUE, value));
        renderer.render(ctx.model());
    }

    private static void parseJsonStringValue(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {

        String value = ctx.jp().getText();
        ctx.model().add(new Element(ElementType.STRING_VALUE, value));
        renderer.render(ctx.model());
    }

    private static void parseJsonNaOrEmbeddedValue(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {

        final String value;

        switch (ctx.token()) {
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

        ctx.model().add(new Element(ElementType.NA_OR_EMBEDDED_VALUE, value));
        renderer.render(ctx.model());
    }

    private static void parseJsonFieldName(ParsingContext ctx, Renderer renderer) throws JsonParseException, IOException {

        ctx.model().add(new Element(ElementType.FIELD_NAME, ctx.jp().getText()));

        renderer.render(ctx.model());
    }

}

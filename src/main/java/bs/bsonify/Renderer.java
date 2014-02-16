package bs.bsonify;

import java.io.IOException;
import java.io.Writer;

/**
 * Example rendering layout: http://p2-dev.pdt-extensions.org/editors.html colors:
 * http://www.open-open.com/projectimage/JsonEditor.jpg
 */
public final class Renderer {

    // http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html
    private static final String BLACK = "\033[30m";
    private static final String BLUE = "\033[34m";
    private static final String GREEN = "\033[32m";
    private static final String CYAN = "\033[36m";
    private static final String RED = "\033[31m";
    private static final String PURPLE = "\033[35m";
    private static final String BROWN = "\033[33m";
    private static final String LIGHT_GRAY = "\033[37m";
    private static final String RESET_COLOR = "\033[0m";

    private static final char OPEN_ARRAY = '[';
    private static final char CLOSE_ARRAY = ']';
    private static final char OPEN_OBJECT = '{';
    private static final char CLOSE_OBJECT = '}';
    private static final char COMMA = ',';
    private static final char COLON = ':';
    private static final char SPACE = ' ';
    private static final char QUOTES = '"';
    private static final char NEWLINE = '\n'; // for console, platform independent is not required

    private static final CharSequence INDENT = "    ";

    public static void render(JsonModel model, Writer target, ColorScheme color) {

        final ElementType prev;

        try {

            // We need two elements in the queue. To render a json element we need access to the previous element
            if (model.toProcess() < 2) {
                prev = ElementType.NONE;
            } else {
                prev = model.poll().getType();
            }

            Element toRender = model.peek();

            switch (toRender.getType()) {
            case START_OBJECT:
                renderOpenOject(prev, toRender, model, target, color);
                break;
            case START_ARRAY:
                renderOpenArray(prev, toRender, model, target, color);
                break;
            case FIELD_NAME:
                renderFieldname(prev, toRender, model, target, color);
                break;
            case STRING_VALUE:
                renderStringValue(prev, toRender, model, target, color);
                break;
            case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
                renderNumberTrueFalseOrNullValue(prev, toRender, model, target, color);
                break;
            case NA_OR_EMBEDDED_VALUE:
                renderNaOrEmbeddedValue(prev, toRender, model, target, color);
                break;
            case END_ARRAY:
                renderEndArray(prev, toRender, model, target, color);
                break;
            case END_OBJECT:
                renderEndObject(prev, toRender, model, target, color);
                break;
            default:
                break;
            }
        } catch (IOException e) {
            throw new RenderException(e);
        }

    }

    private static void renderOpenOject(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {
        switch (prev) {
        case NONE:
            colorSymbol(target, color);
            break;
        case START_OBJECT:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            colorSymbol(target, color);
            break;
        case START_ARRAY:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            colorSymbol(target, color);
            break;
        case FIELD_NAME:
            colorSymbol(target, color);
            break;
        case END_OBJECT:
            colorSymbol(target, color).append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
        case END_ARRAY:
            throwUnexpectedElement(prev, ElementType.START_OBJECT);
        default:
            break;
        }

        target.append(OPEN_OBJECT);
    }

    private static void renderOpenArray(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {
        switch (prev) {
        case NONE:
            colorSymbol(target, color);
            break;
        case START_ARRAY:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case FIELD_NAME:
            colorSymbol(target, color);
            break;
        case END_ARRAY:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case START_OBJECT:
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.START_ARRAY);
        default:
            break;
        }

        target.append(OPEN_ARRAY);
    }

    private static void renderFieldname(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {

        switch (prev) {
        case START_OBJECT:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            colorField(target, color);
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol(target, color).append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            colorField(target, color);
            break;
        case END_ARRAY:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            colorField(target, color);
            break;
        case END_OBJECT:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            colorField(target, color);
            break;
        case NONE:
        case START_ARRAY:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.FIELD_NAME);
        default:
            break;
        }

        appendInQuotes(target, toRender.value());
        target.append(COLON).append(SPACE);
    }

    private static void renderStringValue(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {
        switch (prev) {
        case START_ARRAY:
        case FIELD_NAME:
            colorValue(target, color);
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol(target, color).append(COMMA).append(SPACE);
            colorValue(target, color);
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.STRING_VALUE);
        default:
            break;
        }
        appendInQuotes(target, toRender.value());
    }

    private static void renderNumberTrueFalseOrNullValue(ElementType prev, Element toRender, JsonModel model, Writer target,
            ColorScheme color) throws IOException {
        switch (prev) {
        case START_ARRAY:
            colorValue(target, color);
            break;
        case FIELD_NAME:
            colorValue(target, color);
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol(target, color).append(COMMA).append(SPACE);
            colorValue(target, color);
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.NUMBER_TRUE_FALSE_OR_NULL_VALUE);
        default:
            break;
        }

        target.append(toRender.value());
    }

    private static void renderNaOrEmbeddedValue(ElementType prev, Element toRender, JsonModel model, Writer target,
            ColorScheme color) throws IOException {

        switch (prev) {
        case START_ARRAY:
            colorValue(target, color);
            break;
        case FIELD_NAME:
            colorValue(target, color);
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol(target, color).append(COMMA).append(SPACE);
            colorValue(target, color);
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.NA_OR_EMBEDDED_VALUE);
        default:
            break;
        }

        target.append(toRender.value());
    }

    private static void renderEndArray(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {
        switch (prev) {
        case START_ARRAY:
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol(target, color);
            break;
        case END_ARRAY:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case END_OBJECT:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case NONE:
        case START_OBJECT:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.END_ARRAY);
        default:
            break;
        }

        target.append(CLOSE_ARRAY);
    }

    private static void renderEndObject(ElementType prev, Element toRender, JsonModel model, Writer target, ColorScheme color)
            throws IOException {
        switch (prev) {
        case START_OBJECT:
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            colorSymbol(target, color);
            break;
        case END_ARRAY:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case END_OBJECT:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case NONE:
        case START_ARRAY:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.END_OBJECT);
        default:
            break;
        }

        target.append(CLOSE_OBJECT);
    }

    private static void appendInQuotes(Writer target, CharSequence cs) throws IOException {
        target.append(QUOTES).append(cs).append(QUOTES);
    }

    private static CharSequence indent(int depth) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
        return sb;
    }

    private static Writer colorSymbol(Writer target, ColorScheme color) throws IOException {
        switch (color) {
        case MONO:
            break;
        case DARK:
        case LIGHT:
            target.append(LIGHT_GRAY);
            break;
        default:
            break;
        }
        return target;
    }

    private static Writer colorValue(Writer target, ColorScheme color) throws IOException {
        switch (color) {
        case MONO:
            break;
        case DARK:
        case LIGHT:
            target.append(BLUE);
            break;
        default:
            break;
        }
        return target;
    }

    private static Writer colorField(Writer target, ColorScheme color) throws IOException {
        switch (color) {
        case MONO:
            break;
        case DARK:
        case LIGHT:
            target.append(GREEN);
            break;
        default:
            break;
        }
        return target;
    }

    private static void throwUnexpectedElement(ElementType first, ElementType second) {
        throw new RenderException(String.format("Unexpected JSON elements: %s after a %s", second, first));

    }

    public static void resetColor(Writer target, ColorScheme color) throws IOException {
        if (color == ColorScheme.MONO) {
            return;
        }
        target.write(RESET_COLOR);
    }
}

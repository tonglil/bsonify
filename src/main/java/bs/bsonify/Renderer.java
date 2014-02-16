package bs.bsonify;

import java.io.IOException;
import java.io.Writer;

/**
 * Example rendering layout: http://p2-dev.pdt-extensions.org/editors.html colors:
 * http://www.open-open.com/projectimage/JsonEditor.jpg
 */
public final class Renderer extends Writer {

    // http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html
    private static final String BLACK = "\033[0;30m";
    private static final String BLUE = "\033[0;34m";
    private static final String GREEN = "\033[0;32m";
    private static final String LIGHT_GRAY = "\033[0;37m";
    
    private static final String LIGHT_BLUE = "\033[1;34m";
    private static final String LIGHT_GREEN = "\033[1;32m";
    
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

    private final Writer writer;
    private final Options options;

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public Renderer(Writer writer, Options options) {
        super();
        this.writer = writer;
        this.options = options;
    }

    public void render(JsonModel model) {

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
                renderOpenOject(prev, toRender, model);
                break;
            case START_ARRAY:
                renderOpenArray(prev, toRender, model);
                break;
            case FIELD_NAME:
                renderFieldname(prev, toRender, model);
                break;
            case STRING_VALUE:
                renderStringValue(prev, toRender, model);
                break;
            case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
                renderNumberTrueFalseOrNullValue(prev, toRender, model);
                break;
            case NA_OR_EMBEDDED_VALUE:
                renderNaOrEmbeddedValue(prev, toRender, model);
                break;
            case END_ARRAY:
                renderEndArray(prev, toRender, model);
                break;
            case END_OBJECT:
                renderEndObject(prev, toRender, model);
                break;
            default:
                break;
            }
        } catch (IOException e) {
            throw new RenderException(e);
        }

    }

    private void renderOpenOject(ElementType prev, Element toRender, JsonModel model)
            throws IOException {
        switch (prev) {
        case NONE:
            colorSymbol();
            break;
        case START_OBJECT:
            model.levelDown();
            appendNewLineAndIndent(model);
            colorSymbol();
            break;
        case START_ARRAY:
            model.levelDown();
            appendNewLineAndIndent(model);
            colorSymbol();
            break;
        case FIELD_NAME:
            colorSymbol();
            break;
        case END_OBJECT:
            colorSymbol().append(COMMA);
            appendNewLineAndIndent(model);
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
        case END_ARRAY:
            throwUnexpectedElement(prev, ElementType.START_OBJECT);
        default:
            break;
        }

        append(OPEN_OBJECT);
    }

    private void renderOpenArray(ElementType prev, Element toRender, JsonModel model)
            throws IOException {
        switch (prev) {
        case NONE:
            colorSymbol();
            break;
        case START_ARRAY:
            model.levelDown();
            appendNewLineAndIndent(model);
            break;
        case FIELD_NAME:
            colorSymbol();
            break;
        case END_ARRAY:
            append(COMMA);
            appendNewLineAndIndent(model);
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

        append(OPEN_ARRAY);
    }

    private void renderFieldname(ElementType prev, Element toRender, JsonModel model)
            throws IOException {

        switch (prev) {
        case START_OBJECT:
            model.levelDown();
            appendNewLineAndIndentOnlyIfExpanded(model).colorField();
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol().append(COMMA);
            appendNewLineAndIndentOnlyIfExpanded(model).colorField();
            break;
        case END_ARRAY:
            append(COMMA);
            appendNewLineAndIndent(model);
            colorField();
            break;
        case END_OBJECT:
            append(COMMA);
            appendNewLineAndIndentOnlyIfExpanded(model);
            colorField();
            break;
        case NONE:
        case START_ARRAY:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.FIELD_NAME);
        default:
            break;
        }

        appendInQuotes(toRender.value());
        append(COLON).append(SPACE);
    }
    
    private void renderStringValue(ElementType prev, Element toRender, JsonModel model)
            throws IOException {
        switch (prev) {
        case START_ARRAY:
        case FIELD_NAME:
            colorValue();
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol().append(COMMA).append(SPACE);
            colorValue();
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.STRING_VALUE);
        default:
            break;
        }
        appendInQuotes(toRender.value());
    }

    private void renderNumberTrueFalseOrNullValue(ElementType prev, Element toRender, JsonModel model) throws IOException {
        switch (prev) {
        case START_ARRAY:
            colorValue();
            break;
        case FIELD_NAME:
            colorValue();
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol().append(COMMA).append(SPACE);
            colorValue();
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.NUMBER_TRUE_FALSE_OR_NULL_VALUE);
        default:
            break;
        }

        append(toRender.value());
    }

    private void renderNaOrEmbeddedValue(ElementType prev, Element toRender, JsonModel model) throws IOException {

        switch (prev) {
        case START_ARRAY:
            colorValue();
            break;
        case FIELD_NAME:
            colorValue();
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol().append(COMMA).append(SPACE);
            colorValue();
            break;
        case NONE:
        case START_OBJECT:
        case END_ARRAY:
        case END_OBJECT:
            throwUnexpectedElement(prev, ElementType.NA_OR_EMBEDDED_VALUE);
        default:
            break;
        }

        append(toRender.value());
    }

    private void renderEndArray(ElementType prev, Element toRender, JsonModel model)
            throws IOException {
        switch (prev) {
        case START_ARRAY:
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            colorSymbol();
            break;
        case END_ARRAY:
            model.levelUp();
            appendNewLineAndIndentOnlyIfExpanded(model);
            break;
        case END_OBJECT:
            model.levelUp();
            appendNewLineAndIndentOnlyIfExpanded(model);
            break;
        case NONE:
        case START_OBJECT:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.END_ARRAY);
        default:
            break;
        }

        append(CLOSE_ARRAY);
    }

    private void renderEndObject(ElementType prev, Element toRender, JsonModel model)
            throws IOException {
        switch (prev) {
        case START_OBJECT:
            break;
        case STRING_VALUE:
        case NUMBER_TRUE_FALSE_OR_NULL_VALUE:
        case NA_OR_EMBEDDED_VALUE:
            model.levelUp();
            appendNewLineAndIndentOnlyIfExpanded(model);
            colorSymbol();
            break;
        case END_ARRAY:
            model.levelUp();
            appendNewLineAndIndentOnlyIfExpanded(model);
            break;
        case END_OBJECT:
            model.levelUp();
            appendNewLineAndIndentOnlyIfExpanded(model);
            break;
        case NONE:
        case START_ARRAY:
        case FIELD_NAME:
            throwUnexpectedElement(prev, ElementType.END_OBJECT);
        default:
            break;
        }

        append(CLOSE_OBJECT);
    }

    private void appendInQuotes(CharSequence cs) throws IOException {
        append(QUOTES).append(cs).append(QUOTES);
    }

    private static CharSequence indent(int depth) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
        return sb;
    }

    /** Appends space in compact mode, otherwise in expanded mode appends a newline and the indent */
    private Renderer appendNewLineAndIndentOnlyIfExpanded(JsonModel model) throws IOException {
        if (options.compact()) {
            append(SPACE);
        } else {
            append(NEWLINE).append(indent(model.indentLevel()));
        }
        return this;
    }

    private Renderer appendNewLineAndIndent(JsonModel model) throws IOException {
        append(NEWLINE).append(indent(model.indentLevel()));
        return this;
    }

    private Writer colorSymbol() throws IOException {
        switch (options.color()) {
        case MONO:
            break;
        case DARK:
            append(BLACK);
            break;
        case LIGHT:
            append(LIGHT_GRAY);
            break;
        default:
            break;
        }
        return this;
    }

    private Writer colorValue() throws IOException {
        switch (options.color()) {
        case MONO:
            break;
        case DARK:
            append(BLUE);
            break;
        case LIGHT:
            append(LIGHT_BLUE);
            break;
        default:
            break;
        }
        return this;
    }

    private Writer colorField() throws IOException {
        switch (options.color()) {
        case MONO:
            break;
        case DARK:
            append(GREEN);
            break;
        case LIGHT:
            append(LIGHT_GREEN);
            break;
        default:
            break;
        }
        return this;
    }

    private void throwUnexpectedElement(ElementType first, ElementType second) {
        throw new RenderException(String.format("Unexpected JSON elements: %s after a %s", second, first));

    }

    public void resetColor() throws IOException {
        if (options.color() == ColorScheme.MONO) {
            return;
        }
        write(RESET_COLOR);
    }
}

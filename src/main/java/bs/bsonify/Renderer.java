package bs.bsonify;

import java.io.StringWriter;

/**
 * Example rendering layout: http://p2-dev.pdt-extensions.org/editors.html
 * colors: http://www.open-open.com/projectimage/JsonEditor.jpg
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

    public static void render(JsonModel model, StringWriter target) {
        final ElementType prev;
        
        // We need two elements in the queue. To render a json element we need access to the previous element
        if (model.toProcess() < 2) {
            prev = ElementType.NONE;
        } else {
            prev = model.poll().getType();
        }
        
        Element toRender = model.peek();

        switch (toRender.getType()) {
        case START_OBJECT:
            renderOpenOject(prev, toRender, model, target);
            break;
        case START_ARRAY:
            renderOpenArray(prev, toRender, model, target);
            break;
        case FIELD_NAME:
            renderFieldname(prev, toRender, model, target);
            break;
        case VALUE:
            renderValue(prev, toRender, model, target);
            break;
        case END_ARRAY:
            renderEndArray(prev, toRender, model, target);
            break;
        case END_OBJECT:
            renderEndObject(prev, toRender, model, target);
            break;
        default:
            break;
        }

    }

    private static void renderOpenOject(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            target.append(LIGHT_GRAY);
            break;
        case START_OBJECT:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel())).append(LIGHT_GRAY);
            break;
        case START_ARRAY:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel())).append(LIGHT_GRAY);
            break;
        case FIELD_NAME:
            target.append(LIGHT_GRAY);
            break;
        case VALUE:
            throwUnexpectedElement(ElementType.VALUE, ElementType.START_OBJECT);
            break;
        case END_ARRAY:
            throwUnexpectedElement(ElementType.END_ARRAY, ElementType.START_OBJECT);
            break;
        case END_OBJECT:
            target.append(LIGHT_GRAY).append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            break;

        default:
            break;
        }

        target.append(OPEN_OBJECT);
    }

    private static void throwUnexpectedElement(ElementType first, ElementType second) {
        throw new RenderException(String.format("Unexpected JSON elements: %s after a %s", second, first));

    }

    private static void renderOpenArray(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            target.append(LIGHT_GRAY);
            break;
        case START_OBJECT:
            throwUnexpectedElement(ElementType.START_OBJECT, ElementType.START_ARRAY);
            break;
        case START_ARRAY:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case FIELD_NAME:
            target.append(LIGHT_GRAY);
            break;
        case VALUE:
            throwUnexpectedElement(ElementType.VALUE, ElementType.START_ARRAY);
            break;
        case END_ARRAY:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case END_OBJECT:
            throwUnexpectedElement(ElementType.END_OBJECT, ElementType.START_ARRAY);
            break;
        default:
            break;
        }

        target.append(OPEN_ARRAY);
    }

    private static void renderFieldname(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            throwUnexpectedElement(ElementType.NONE, ElementType.FIELD_NAME);
        case START_OBJECT:
            model.levelDown();
            target.append(NEWLINE).append(indent(model.indentLevel())).append(GREEN);
            break;
        case START_ARRAY:
            throwUnexpectedElement(ElementType.START_ARRAY, ElementType.FIELD_NAME);
        case FIELD_NAME:
            throwUnexpectedElement(ElementType.FIELD_NAME, ElementType.FIELD_NAME);
        case VALUE:
            target.append(LIGHT_GRAY).append(COMMA).append(NEWLINE).append(indent(model.indentLevel())).append(GREEN);
            break;
        case END_ARRAY:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel())).append(GREEN);
            break;
        case END_OBJECT:
            target.append(COMMA).append(NEWLINE).append(indent(model.indentLevel())).append(GREEN);
            break;
        default:
            break;
        }

        appendInQuotes(target, toRender.value());
        target.append(COLON).append(SPACE);
    }

    private static void renderValue(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            throwUnexpectedElement(ElementType.NONE, ElementType.VALUE);
        case START_OBJECT:
            throwUnexpectedElement(ElementType.START_OBJECT, ElementType.VALUE);
            break;
        case START_ARRAY:
            target.append(BLUE);
            break;
        case FIELD_NAME:
            target.append(BLUE);
            break;
        case VALUE:
            target.append(LIGHT_GRAY).append(COMMA).append(SPACE).append(BLUE);
            break;
        case END_ARRAY:
            throwUnexpectedElement(ElementType.END_ARRAY, ElementType.VALUE);
            break;
        case END_OBJECT:
            throwUnexpectedElement(ElementType.END_OBJECT, ElementType.VALUE);
            break;
        default:
            break;
        }

        appendInQuotes(target, toRender.value());
    }

    private static void renderEndArray(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            throwUnexpectedElement(ElementType.NONE, ElementType.END_ARRAY);
        case START_OBJECT:
            throwUnexpectedElement(ElementType.START_OBJECT, ElementType.END_ARRAY);
            break;
        case START_ARRAY:
            break;
        case FIELD_NAME:
            throwUnexpectedElement(ElementType.FIELD_NAME, ElementType.END_ARRAY);
            break;
        case VALUE:
            target.append(LIGHT_GRAY);
            break;
        case END_ARRAY:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case END_OBJECT:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        default:
            break;
        }

        target.append(CLOSE_ARRAY);
    }

    private static void renderEndObject(ElementType prev, Element toRender, JsonModel model, StringWriter target) {
        switch (prev) {
        case NONE:
            throwUnexpectedElement(ElementType.NONE, ElementType.END_OBJECT);
        case START_OBJECT:
            break;
        case START_ARRAY:
            throwUnexpectedElement(ElementType.START_ARRAY, ElementType.END_OBJECT);
            break;
        case FIELD_NAME:
            throwUnexpectedElement(ElementType.FIELD_NAME, ElementType.END_OBJECT);
            break;
        case VALUE:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel())).append(LIGHT_GRAY);
            break;
        case END_ARRAY:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        case END_OBJECT:
            model.levelUp();
            target.append(NEWLINE).append(indent(model.indentLevel()));
            break;
        default:
            break;
        }

        target.append(CLOSE_OBJECT);
    }

    private static void appendInQuotes(StringWriter target, CharSequence cs) {
        target.append(QUOTES).append(cs).append(QUOTES);
    }

    private static CharSequence indent(int depth) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
        return sb;
    }

    public static void resetColor(StringWriter target) {
        target.write(RESET_COLOR);
    }

}

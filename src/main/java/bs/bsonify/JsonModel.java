package bs.bsonify;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a part of JSON content beeing rendered.
 */
public class JsonModel {
    private int indentLevel;
    
    private final Queue<Element> elements = new LinkedList<>();

    public void add(Element el) {
        elements.add(el);
    }

    public Element poll() {
        return elements.poll();
    }

    public Element peek() {
        return elements.peek();
    }

    public int toProcess() {
        return elements.size();
    }

    public int indentLevel() {
        return indentLevel;
    }

    public void levelDown() {
        indentLevel++;
    }

    public void levelUp() {
        indentLevel--;
    }
}

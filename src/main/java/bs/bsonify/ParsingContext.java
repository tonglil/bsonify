package bs.bsonify;

import java.io.StringWriter;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class ParsingContext {

    private JsonToken token;
    private final JsonParser jp;
    private final StringWriter target;
    private final JsonModel model;
    private int jsonCharsCount = 0;;

    public ParsingContext(JsonParser jp, StringWriter target, JsonToken token, JsonModel model) {
        super();
        this.token = token;
        this.jp = jp;
        this.target = target;
        this.model = model;
    }

    public JsonToken token() {
        return token;
    }

    public void setToken(JsonToken token) {
        this.token = token;
    }

    public JsonParser jp() {
        return jp;
    }

    public StringWriter target() {
        return target;
    }

    public JsonModel model() {
        return model;
    }

    public void incrementJsonCharCount(int jsonCharsRead) {
        jsonCharsCount += jsonCharsRead;
    }

    public int jsonCharsCount() {
        return jsonCharsCount;
    }
}

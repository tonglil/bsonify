package bs.bsonify;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class ParsingContext {

    private JsonToken token;
    private final JsonParser jp;
    private final JsonModel model;

    public ParsingContext(JsonParser jp, JsonToken token, JsonModel model) {
        super();
        this.token = token;
        this.jp = jp;
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

    public JsonModel model() {
        return model;
    }
}

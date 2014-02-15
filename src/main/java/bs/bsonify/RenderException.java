package bs.bsonify;

public class RenderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RenderException(String message) {
        super(message);
    }

    public RenderException(Throwable cause) {
        super(cause);
    }

}

package bs.bsonify;

public class Options {
    private ColorScheme color = ColorScheme.LIGHT;
    private boolean compact;
    private String filename;

    public String filename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean hasFilename() {
        return filename != null;
    }

    public ColorScheme color() {
        return color;
    }

    public void setColor(ColorScheme color) {
        this.color = color;
    }

    public boolean compact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }
}

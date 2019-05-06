/**
 * This stores the info for a given symbol line in a symbol table.
 */
public class SymbolLine {
    String type;
    String kind;
    Integer index;

    public SymbolLine(String type, String kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return kind + " " + index.toString();
    }

    public String  getPop()
    {
        return "pop " + this.toString();
    }

    public String  getPush()
    {
        return "push " + this.toString();
    }
}

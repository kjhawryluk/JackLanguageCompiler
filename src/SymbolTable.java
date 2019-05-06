import java.util.Hashtable;

/**
 * This is a symbol table used for tacking the scope and type of each symbol.
 */
class SymbolTable {
    private Hashtable<String, SymbolLine> table = new Hashtable<>();
    private Hashtable<String, Integer> indexTracker = new Hashtable<>();

    //Adds line to symbol table.
    void addLine(String symbol, String type, String kind)
    {
        symbol = Compiler.getTokenFromLine(symbol);
        type = Compiler.getTokenFromLine(type);
        kind = Compiler.getTokenFromLine(kind);

        //Change var to local to map to local memory segment in vm.
        kind = getVmKind(kind);

        table.put(symbol, new SymbolLine(type, kind, getIndex(kind)));
    }

    //Translates jack kind to vm.
    private String getVmKind(String kind) {
        if(kind.equals("var"))
        {
            kind = "local";
        }
        else if(kind.equals("field"))
        {
            kind = "this";
        }
        return kind;
    }

    //Checks if symbol in Symbol table.
    public boolean hasSymbol(String symbol)
    {
        return table.containsKey(symbol);
    }

    //Looks at indexTracker for local var count. Returns 0 if nothing found.
    public Integer getVarCount(String kind) {
        if(indexTracker.containsKey(kind))
        {
            return indexTracker.get(kind) + 1;
        }
        else
        {
            return 0;
        }
    }

    //Returns symbol line from symbol table.
    public SymbolLine getLine(String symbol)
    {
        return table.get(symbol);
    }

    //Increments index tracker and returns index for this type.
    private int getIndex(String kind)
    {
        int newIndex;
        if(indexTracker.containsKey(kind))
        {
            newIndex = indexTracker.get(kind) + 1;
        }
        else
        {
            newIndex = 0;
        }
        indexTracker.put(kind, newIndex);
        return  newIndex;
    }
}

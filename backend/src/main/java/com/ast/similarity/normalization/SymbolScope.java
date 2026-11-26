package com.ast.similarity.normalization;

import java.util.*;

public class SymbolScope {
    private final SymbolScope parent;
    private final Map<String, String> symbolMap = new HashMap<>();
    private final Map<String, String> symbolKind = new HashMap<>(); // PARAMETER or VARIABLE
    private int paramCounter = 0;
    private int varCounter = 0;

    // Common standard library and keyword symbols that should never be canonicalized as local variables
    private static final Set<String> PRESERVED_SYMBOLS = new HashSet<>(Arrays.asList(
            "main", "printf", "scanf", "malloc", "free", "exit", "strlen", "strcpy", "strcmp",
            "cout", "cin", "endl", "std", "string", "vector", "map", "set", "push_back",
            "print", "range", "len", "open", "input", "int", "str", "float", "list", "dict",
            "sum", "min", "max", "abs", "enumerate", "zip", "append", "pop"
    ));

    public SymbolScope(SymbolScope parent) {
        this.parent = parent;
    }

    public boolean isPreserved(String name) {
        return name == null || PRESERVED_SYMBOLS.contains(name);
    }

    public String registerParameter(String name) {
        if ("main".equals(name)) return name;
        if (!symbolMap.containsKey(name)) {
            String canonical = "param_" + (paramCounter++);
            symbolMap.put(name, canonical);
            symbolKind.put(name, "PARAMETER");
        }
        return symbolMap.get(name);
    }

    public String registerVariable(String name) {
        if ("main".equals(name)) return name;
        if (!symbolMap.containsKey(name)) {
            String canonical = "var_" + (varCounter++);
            symbolMap.put(name, canonical);
            symbolKind.put(name, "VARIABLE");
        }
        return symbolMap.get(name);
    }

    public String resolve(String name) {
        if (symbolMap.containsKey(name)) {
            return symbolMap.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null; // Not found in local symbol table
    }

    public Map<String, String> getSymbolMap() {
        return Collections.unmodifiableMap(symbolMap);
    }

    public Map<String, String> getSymbolKind() {
        return Collections.unmodifiableMap(symbolKind);
    }
}

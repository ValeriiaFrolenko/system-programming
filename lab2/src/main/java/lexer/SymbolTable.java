package lexer;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Integer> nameToCode = new LinkedHashMap<>();

    public int codeFor(String name) {
        return nameToCode.computeIfAbsent(name, _ -> nameToCode.size());
    }

    public boolean contains(String name) {
        return nameToCode.containsKey(name);
    }

    public Map<String, Integer> entries() {
        return Map.copyOf(nameToCode);
    }
}
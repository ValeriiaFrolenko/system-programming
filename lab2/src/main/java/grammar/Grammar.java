package grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Незмінна контекстно-вільна граматика: стартовий символ і список продукцій.
 *
 * <p>Граматика - це звичайні дані. Вона нічого не знає про розбір; парсер
 * читає її через {@link #start()}, {@link #productions()} і
 * {@link #productionsFor(NonTerminal)}.
 *
 * <p>Граматика перевіряється під час побудови:
 * <ul>
 *   <li>має принаймні одну продукцію;</li>
 *   <li>стартовий символ має продукції;</li>
 *   <li>кожен нетермінал, використаний у правій частині, має продукції;</li>
 *   <li>коди й назви нетерміналів унікальні.</li>
 * </ul>
 * Порушення повідомляються через {@link GrammarException}.
 */
public final class Grammar {
    private final NonTerminal start;
    private final List<Production> productions;
    private final Map<NonTerminal, List<Production>> productionsByLeft;

    private Grammar(NonTerminal start, List<Production> productions) {
        if (productions.isEmpty()) {
            throw new GrammarException("Grammar has no productions");
        }
        this.start = start;
        this.productions = List.copyOf(productions);

        Map<NonTerminal, List<Production>> grouped = new LinkedHashMap<>();
        for (Production production : this.productions) {
            grouped.computeIfAbsent(production.left(), key -> new ArrayList<>()).add(production);
        }
        grouped.replaceAll((key, list) -> List.copyOf(list));
        this.productionsByLeft = Collections.unmodifiableMap(grouped);

        validate();
    }

    /**
     * Починає побудову граматики із заданим стартовим символом.
     */
    public static Builder builder(NonTerminal start) {
        return new Builder(start);
    }

    /**
     * Повертає стартовий символ.
     */
    public NonTerminal start() {
        return start;
    }

    /**
     * Повертає всі продукції в порядку оголошення.
     */
    public List<Production> productions() {
        return productions;
    }

    /**
     * Повертає продукції одного нетермінала або порожній список, якщо їх немає.
     */
    public List<Production> productionsFor(NonTerminal nonTerminal) {
        return productionsByLeft.getOrDefault(nonTerminal, List.of());
    }

    /**
     * Повертає всі визначені нетермінали в порядку оголошення.
     */
    public Set<NonTerminal> nonTerminals() {
        return productionsByLeft.keySet();
    }

    private void validate() {
        if (!productionsByLeft.containsKey(start)) {
            throw new GrammarException("Start symbol " + start + " has no productions");
        }

        Map<Integer, NonTerminal> byCode = new HashMap<>();
        Set<String> names = new HashSet<>();
        for (NonTerminal nonTerminal : productionsByLeft.keySet()) {
            NonTerminal sameCode = byCode.put(nonTerminal.code(), nonTerminal);
            if (sameCode != null) {
                throw new GrammarException("Non-terminals " + sameCode + " and " + nonTerminal
                        + " share the code " + nonTerminal.code());
            }
            if (!names.add(nonTerminal.name())) {
                throw new GrammarException("Duplicate non-terminal name: " + nonTerminal.name());
            }
        }

        for (Production production : productions) {
            for (Symbol symbol : production.right()) {
                if (symbol instanceof NonTerminal nonTerminal && !productionsByLeft.containsKey(nonTerminal)) {
                    throw new GrammarException("Non-terminal " + nonTerminal + " is used in '"
                            + production + "' but has no productions");
                }
            }
        }
    }

    /**
     * Fluent-побудовник для {@link Grammar}.
     */
    public static final class Builder {
        private final NonTerminal start;
        private final List<Production> productions = new ArrayList<>();

        private Builder(NonTerminal start) {
            this.start = start;
        }

        /**
         * Додає продукцію {@code left ::= right...}.
         * Викличте без символів {@code right}, щоб додати епсілон-продукцію.
         */
        public Builder rule(NonTerminal left, Symbol... right) {
            productions.add(new Production(left, List.of(right)));
            return this;
        }

        /**
         * Будує й перевіряє граматику.
         */
        public Grammar build() {
            return new Grammar(start, productions);
        }
    }
}

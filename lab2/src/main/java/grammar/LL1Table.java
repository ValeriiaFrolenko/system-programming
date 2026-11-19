package grammar;

import lexer.TokenType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Таблиця розбору LL(1): для нетермінала на вершині стека парсера й
 * наступного токена дає продукцію, якою слід розгортати.
 *
 * <p>Продукція {@code A ::= alpha} розміщується в клітинці {@code [A, t]} для
 * кожного термінала {@code t} з FIRST(alpha) і, якщо alpha може вивести
 * порожній рядок, для кожного термінала з FOLLOW(A). Якщо дві продукції
 * одного нетермінала претендують на одну клітинку, граматика не LL(1),
 * і конструктор кидає {@link GrammarException}.
 */
public final class LL1Table {
    private final Map<NonTerminal, Map<TokenType, Production>> cells = new HashMap<>();

    /**
     * Будує таблицю.
     *
     * @throws GrammarException якщо граматика не є LL(1)
     */
    public LL1Table(Grammar grammar, FirstFollowSets sets) {
        for (NonTerminal nonTerminal : grammar.nonTerminals()) {
            cells.put(nonTerminal, new EnumMap<>(TokenType.class));
        }

        for (Production production : grammar.productions()) {
            Set<TokenType> lookahead = EnumSet.noneOf(TokenType.class);
            lookahead.addAll(sets.firstOf(production.right()));
            if (sets.isNullable(production.right())) {
                lookahead.addAll(sets.follow(production.left()));
            }

            Map<TokenType, Production> row = cells.get(production.left());
            for (TokenType token : lookahead) {
                Production previous = row.putIfAbsent(token, production);
                if (previous != null) {
                    throw new GrammarException("Grammar is not LL(1): conflict for "
                            + production.left() + " on " + token + " between '"
                            + previous + "' and '" + production + "'");
                }
            }
        }
    }

    /**
     * Повертає продукцію для нетермінала й наступного токена, якщо вона є.
     */
    public Optional<Production> lookup(NonTerminal nonTerminal, TokenType token) {
        Map<TokenType, Production> row = cells.get(nonTerminal);
        return row == null ? Optional.empty() : Optional.ofNullable(row.get(token));
    }

    /**
     * Повертає токени, що можуть починати або йти після нетермінала, для
     * повідомлень про помилки.
     */
    public Set<TokenType> expected(NonTerminal nonTerminal) {
        Map<TokenType, Production> row = cells.get(nonTerminal);
        return row == null ? Set.of() : Collections.unmodifiableSet(row.keySet());
    }
}

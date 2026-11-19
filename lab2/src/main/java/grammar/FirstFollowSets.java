package grammar;

import lexer.TokenType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Множини nullable, FIRST і FOLLOW для {@link Grammar}.
 *
 * <p>Усі множини обчислюються один раз, у конструкторі, методом нерухомої точки:
 * <ul>
 *   <li><b>nullable</b> - нетермінали, з яких можна вивести порожній рядок;</li>
 *   <li><b>FIRST(A)</b> - термінали, з яких може починатися рядок, виведений із {@code A};</li>
 *   <li><b>FOLLOW(A)</b> - термінали, що можуть стояти одразу після {@code A}.
 *       {@link TokenType#EOF} входить у FOLLOW стартового символу.</li>
 * </ul>
 */
public final class FirstFollowSets {
    private final Set<NonTerminal> nullable = new HashSet<>();
    private final Map<NonTerminal, Set<TokenType>> first = new HashMap<>();
    private final Map<NonTerminal, Set<TokenType>> follow = new HashMap<>();

    /**
     * Обчислює всі множини для заданої граматики.
     */
    public FirstFollowSets(Grammar grammar) {
        for (NonTerminal nonTerminal : grammar.nonTerminals()) {
            first.put(nonTerminal, EnumSet.noneOf(TokenType.class));
            follow.put(nonTerminal, EnumSet.noneOf(TokenType.class));
        }
        computeNullable(grammar);
        computeFirst(grammar);
        computeFollow(grammar);
    }

    /**
     * Повертає {@code true}, якщо з нетермінала можна вивести порожній рядок.
     */
    public boolean isNullable(NonTerminal nonTerminal) {
        return nullable.contains(nonTerminal);
    }

    /**
     * Повертає {@code true}, якщо кожен символ послідовності може вивести порожній рядок.
     */
    public boolean isNullable(List<Symbol> sequence) {
        for (Symbol symbol : sequence) {
            if (!(symbol instanceof NonTerminal nonTerminal) || !nullable.contains(nonTerminal)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Повертає FIRST нетермінала як незмінну множину.
     */
    public Set<TokenType> first(NonTerminal nonTerminal) {
        return Collections.unmodifiableSet(known(first, nonTerminal));
    }

    /**
     * Повертає FOLLOW нетермінала як незмінну множину.
     */
    public Set<TokenType> follow(NonTerminal nonTerminal) {
        return Collections.unmodifiableSet(known(follow, nonTerminal));
    }

    /**
     * Повертає FIRST послідовності символів: термінали, з яких може починатися
     * рядок, виведений із неї. Результат - нова змінна множина; порожня
     * послідовність дає порожню множину.
     */
    public Set<TokenType> firstOf(List<Symbol> sequence) {
        Set<TokenType> result = EnumSet.noneOf(TokenType.class);
        for (Symbol symbol : sequence) {
            switch (symbol) {
                case Terminal terminal -> {
                    result.add(terminal.type());
                    return result;
                }
                case NonTerminal nonTerminal -> {
                    result.addAll(first.get(nonTerminal));
                    if (!nullable.contains(nonTerminal)) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    private void computeNullable(Grammar grammar) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production production : grammar.productions()) {
                if (!nullable.contains(production.left()) && isNullable(production.right())) {
                    nullable.add(production.left());
                    changed = true;
                }
            }
        }
    }

    private void computeFirst(Grammar grammar) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production production : grammar.productions()) {
                changed |= first.get(production.left()).addAll(firstOf(production.right()));
            }
        }
    }

    private void computeFollow(Grammar grammar) {
        follow.get(grammar.start()).add(TokenType.EOF);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production production : grammar.productions()) {
                List<Symbol> right = production.right();
                for (int i = 0; i < right.size(); i++) {
                    if (!(right.get(i) instanceof NonTerminal target)) {
                        continue;
                    }
                    List<Symbol> rest = right.subList(i + 1, right.size());
                    changed |= follow.get(target).addAll(firstOf(rest));
                    if (isNullable(rest)) {
                        changed |= follow.get(target).addAll(follow.get(production.left()));
                    }
                }
            }
        }
    }

    private static Set<TokenType> known(Map<NonTerminal, Set<TokenType>> sets, NonTerminal nonTerminal) {
        return Objects.requireNonNull(sets.get(nonTerminal), () -> "Unknown non-terminal: " + nonTerminal);
    }
}

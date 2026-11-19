package grammar;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Одне правило граматики {@code left ::= right}.
 *
 * <p>Порожня права частина - це епсілон-продукція.
 *
 * @param left  нетермінал, що визначається
 * @param right символи, на які розгортається нетермінал; копіюються захисно
 */
public record Production(NonTerminal left, List<Symbol> right) {

    public Production {
        Objects.requireNonNull(left, "left");
        right = List.copyOf(right);
    }

    /**
     * Повертає {@code true}, якщо права частина порожня.
     */
    public boolean isEpsilon() {
        return right.isEmpty();
    }

    @Override
    public String toString() {
        String body = isEpsilon()
                ? "<empty>"
                : right.stream().map(Symbol::toString).collect(Collectors.joining(" "));
        return left + " ::= " + body;
    }
}

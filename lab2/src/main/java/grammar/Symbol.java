package grammar;

/**
 * Символ граматики: або {@link Terminal}, або {@link NonTerminal}.
 *
 * <p>Права частина кожної {@link Production} - це послідовність символів.
 */
public sealed interface Symbol permits Terminal, NonTerminal {
}

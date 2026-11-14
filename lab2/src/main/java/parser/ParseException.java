package parser;

import lexer.Token;

/**
 * Помилка синтаксичного аналізу.
 *
 * <p>Виникає, коли {@link Parser} натрапляє на токен, якого не очікував
 * у поточному стані (наприклад, забракло коми, дужки або {@code #} в
 * кінці тексту). Містить точну позицію токена, на якому стався збій.
 */
public class ParseException extends RuntimeException {
    public ParseException(Token token, String message) {
        super("Синтаксична помилка на рядку " + token.line() + ", стовпці "
                + token.column() + ": " + message
                + " (знайдено: '" + token.text() + "')");
    }
}

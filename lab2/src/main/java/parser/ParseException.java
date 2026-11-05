package parser;

import lexer.Token;

public class ParseException extends RuntimeException {
    public ParseException(Token token, String message) {
        super("Синтаксична помилка на рядку " + token.line() + ", стовпці " + token.column() + ": " + message + " (знайдено: '" + token.text() + "')");
    }
}
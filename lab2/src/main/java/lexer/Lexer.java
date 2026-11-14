package lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Лексичний аналізатор.
 *
 * <p>Перетворює вихідний текст на послідовність токенів ({@link Token}).
 * Класифікацію ключових слів делегує {@link KeywordTable}, тож сам
 * займається лише скануванням символів: числа, розділові знаки,
 * послідовності літер, відстеження позиції.
 *
 * <p>Ідентифікатори — лише латиниця, реєстронезалежно; код береться з
 * {@link SymbolTable} і зберігається прямо в токені
 * ({@link Token#code()}). Кінець тексту — символ {@code #}. Останнім у
 * потоці завжди йде {@link TokenType#EOF}.
 */
public class Lexer {
    private final String source;
    private final SymbolTable symbolTable;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    /** Створює лексер із власною таблицею ідентифікаторів. */
    public Lexer(String source) {
        this(source, new SymbolTable());
    }

    /** Створює лексер із зовнішньою таблицею (для повторного використання). */
    public Lexer(String source, SymbolTable symbolTable) {
        this.source = source;
        this.symbolTable = symbolTable;
    }

    public SymbolTable symbolTable() { return symbolTable; }

    /** Повертає всі токени тексту, завершуючи потоком {@link TokenType#EOF}. */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            if (isAtEnd()) {
                tokens.add(new Token(TokenType.EOF, "", line, column));
                return tokens;
            }
            tokens.add(nextToken());
        }
    }

    /** Розпізнає один токен з поточної позиції (без пропусків). */
    private Token nextToken() {
        int startLine = line;
        int startCol = column;
        char c = peek();

        if (c == '(') { advance(); return new Token(TokenType.LPAREN, "(", startLine, startCol); }
        if (c == ')') { advance(); return new Token(TokenType.RPAREN, ")", startLine, startCol); }
        if (c == ',') { advance(); return new Token(TokenType.COMMA, ",", startLine, startCol); }
        if (c == ';') { advance(); return new Token(TokenType.SEMICOLON, ";", startLine, startCol); }
        if (c == '#') { advance(); return new Token(TokenType.END, "#", startLine, startCol); }
        if (Character.isDigit(c) || c == '-' || c == '+') return readNumber();
        if (Character.isLetter(c)) return readWordOrIdentifier();

        throw new LexException(startLine, startCol, "Неочікуваний символ: '" + c + "'");
    }

    /** Читає число: [+-]?digit+(.digit+)?. */
    private Token readNumber() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        if (peek() == '-' || peek() == '+') advance();
        if (!Character.isDigit(peek())) throw new LexException(startLine, startCol, "Очікувалась цифра");

        while (Character.isDigit(peek())) advance();

        if (peek() == '.') {
            advance();
            if (!Character.isDigit(peek())) throw new LexException(startLine, startCol, "Очікувалась цифра після десяткової точки");
            while (Character.isDigit(peek())) advance();
        }

        return new Token(TokenType.NUMBER, source.substring(start, pos), startLine, startCol);
    }

    /** Читає або ключове слово (через {@link KeywordTable}), або ідентифікатор. */
    private Token readWordOrIdentifier() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        while (!isAtEnd() && Character.isLetter(peek())) advance();

        String text = source.substring(start, pos);
        String upper = text.toUpperCase(Locale.ROOT);

        TokenType keyword = KeywordTable.classify(upper);
        if (keyword != null) {
            return new Token(keyword, text, startLine, startCol);
        }

        if (isLatin(text)) {
            int code = symbolTable.codeFor(upper);
            return new Token(TokenType.IDENTIFIER, text, startLine, startCol, code);
        }

        throw new LexException(startLine, startCol, "Невідоме слово: '" + text + "'");
    }

    /** Чи складається рядок лише з латинських літер (обидва регістри). */
    private static boolean isLatin(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) return false;
        }
        return true;
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) advance();
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private void advance() {
        if (isAtEnd()) return;
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else if (c == '\r') {
            if (!isAtEnd() && source.charAt(pos) == '\n') pos++;
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }
}

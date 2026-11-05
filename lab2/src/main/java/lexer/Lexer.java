package lexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Lexer {

    // Стеми відсортовані за спаданням довжини, щоб уникнути колізій префіксів
    // (напр. "ПО" не повинно перехоплювати "ПОБУДУВАТИ", "В" — "ВЕРШИНАМ").
    private static final List<Map.Entry<String, TokenType>> KEYWORDS;

    static {
        Map<String, TokenType> map = new LinkedHashMap<>();
        map.put("ПОБУДУВА", TokenType.BUILD);
        map.put("ПЕРПЕНДИКУЛЯР", TokenType.PERPENDICULAR);
        map.put("ТРИКУТНИК", TokenType.TRIANGLE);
        map.put("ПРОВЕСТ", TokenType.DRAW);
        map.put("ВЕРШИН", TokenType.VERTEX);
        map.put("ТОЧЦ", TokenType.POINT_LOC);
        map.put("ТОЧК", TokenType.POINT);
        map.put("ТРЬОМ", TokenType.THREE);
        map.put("ПРЯМ", TokenType.LINE);
        map.put("ЧЕРЕЗ", TokenType.THROUGH);
        map.put("ДО", TokenType.TO);
        map.put("ПО", TokenType.BY);
        map.put("В", TokenType.AT);

        KEYWORDS = new ArrayList<>(map.entrySet());
        KEYWORDS.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
    }

    private final String source;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String source) {
        this.source = source;
    }

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

    private Token nextToken() {
        int startLine = line;
        int startCol = column;
        char c = peek();

        if (c == '(') { advance(); return new Token(TokenType.LPAREN, "(", startLine, startCol); }
        if (c == ')') { advance(); return new Token(TokenType.RPAREN, ")", startLine, startCol); }
        if (c == ',') { advance(); return new Token(TokenType.COMMA, ",", startLine, startCol); }
        if (Character.isDigit(c) || c == '-' || c == '+') return readNumber();
        if (Character.isLetter(c)) return readWordOrIdentifier();

        throw new LexException(startLine, startCol, "Неочікуваний символ: '" + c + "'");
    }

    private Token readNumber() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        if (peek() == '-' || peek() == '+') {
            advance();
        }
        if (!Character.isDigit(peek())) {
            throw new LexException(startLine, startCol, "Очікувалась цифра");
        }
        while (Character.isDigit(peek())) {
            advance();
        }
        if (peek() == '.') {
            advance();
            if (!Character.isDigit(peek())) {
                throw new LexException(startLine, startCol, "Очікувалась цифра після десяткової точки");
            }
            while (Character.isDigit(peek())) {
                advance();
            }
        }

        String text = source.substring(start, pos);
        return new Token(TokenType.NUMBER, text, startLine, startCol);
    }

    private Token readWordOrIdentifier() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        while (!isAtEnd() && Character.isLetter(peek())) {
            advance();
        }

        String text = source.substring(start, pos);
        String upper = text.toUpperCase(Locale.ROOT);

        for (Map.Entry<String, TokenType> entry : KEYWORDS) {
            if (upper.startsWith(entry.getKey())) {
                return new Token(entry.getValue(), text, startLine, startCol);
            }
        }

        if (isLatin(text)) {
            return new Token(TokenType.IDENTIFIER, text, startLine, startCol);
        }

        throw new LexException(startLine, startCol, "Невідоме слово: '" + text + "'");
    }

    private static boolean isLatin(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }
        return true;
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private void advance() {
        if (isAtEnd()) {
            return;
        }
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else if (c == '\r') {
            if (!isAtEnd() && source.charAt(pos) == '\n') {
                pos++;
            }
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
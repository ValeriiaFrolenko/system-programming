package vfrolenko;

import java.util.Set;

public class Lexer {

    private static final Set<Character> UKRAINIAN_ALLOWED = Set.of(
            'Ф','ф','Р','р','О','о','Л','л','Е','е','Н','н','К','к'
    );

    private static final Set<String> KEYWORDS = Set.of(
            "if", "else", "while", "for", "return", "true", "false",
            "int", "float", "string", "void", "do", "break", "continue"
    );

    private static final Set<String> BUILTIN_FUNCTIONS = Set.of(
            "sin", "cos", "tan", "sqrt", "abs", "log", "exp", "pow",
            "floor", "ceil", "round", "min", "max"
    );

    private final String source;
    private int pos;
    private int line;
    private int column;

    public Lexer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.column = 1;
    }

    public Token nextToken() {
        skipWhitespace();

        if (pos >= source.length()) {
            return new Token(TokenType.EOF, "", line, column);
        }

        char c = peek();

        if (c == '/' && peekNext() == '/') {
            return readLineComment();
        }
        if (c == '"') {
            return readString();
        }
        if (isAsciiDigit(c)) {
            return readNumber();
        }
        if (isIdentStart(c)) {
            return readIdentifierOrKeyword();
        }
        return readOperatorOrDelimiter();
    }

    private Token readLineComment() {
        int startLine = line;
        int startCol  = column;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && peek() != '\n' && peek() != '\r') {
            sb.append(consume());
        }
        return new Token(TokenType.COMMENT, sb.toString(), startLine, startCol);
    }

    private Token readString() {
        int startLine = line;
        int startCol  = column;
        StringBuilder sb = new StringBuilder();
        consume();
        while (pos < source.length()) {
            char c = peek();
            if (c == '"') {
                consume();
                return new Token(TokenType.STRING, sb.toString(), startLine, startCol);
            }
            if (c == '\n' || c == '\r') {
                break;
            }
            if (c == '\\' && pos + 1 < source.length()
                    && peekNext() != '\n' && peekNext() != '\r') {
                consume();
                char esc = consume();
                switch (esc) {
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default   -> sb.append('\\').append(esc);
                }
            } else {
                sb.append(consume());
            }
        }
        return new Token(TokenType.ERROR,
                "Unterminated string literal: \"" + sb, startLine, startCol);
    }

    private Token readNumber() {
        int startLine = line;
        int startCol  = column;
        StringBuilder sb = new StringBuilder();
        boolean isFloat  = false;
        boolean isError  = false;

        while (pos < source.length() && isAsciiDigit(peek())) {
            sb.append(consume());
        }

        if (pos < source.length() && peek() == '.') {
            if (pos + 1 < source.length() && isAsciiDigit(source.charAt(pos + 1))) {
                isFloat = true;
                do {
                    sb.append(consume());
                } while (pos < source.length() && isAsciiDigit(peek()));
                if (pos < source.length() && peek() == '.') {
                    do {
                        sb.append(consume());
                    } while (pos < source.length() && (isAsciiDigit(peek()) || peek() == '.'));
                    isError = true;
                }
            }
        }

        if (pos < source.length() && isIdentStart(peek())) {
            do {
                sb.append(consume());
            } while (pos < source.length() && (isIdentStart(peek()) || isAsciiDigit(peek())));
            isError = true;
        }

        if (isError) {
            return new Token(TokenType.ERROR,
                    "Invalid number literal: " + sb, startLine, startCol);
        }
        return new Token(isFloat ? TokenType.FLOAT : TokenType.INTEGER,
                sb.toString(), startLine, startCol);
    }

    private Token readIdentifierOrKeyword() {
        int startLine = line;
        int startCol  = column;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && (isIdentStart(peek()) || isAsciiDigit(peek()))) {
            sb.append(consume());
        }

        String word = sb.toString();

        if (KEYWORDS.contains(word)) {
            return new Token(TokenType.KEYWORD, word, startLine, startCol);
        }
        if (BUILTIN_FUNCTIONS.contains(word)) {
            return new Token(TokenType.BUILTIN_FUNCTION, word, startLine, startCol);
        }
        return new Token(TokenType.IDENTIFIER, word, startLine, startCol);
    }

    private Token readOperatorOrDelimiter() {
        int startLine = line;
        int startCol  = column;
        char c = consume();

        return switch (c) {
            case '+' -> new Token(TokenType.PLUS,      "+", startLine, startCol);
            case '-' -> new Token(TokenType.MINUS,     "-", startLine, startCol);
            case '*' -> new Token(TokenType.MULTIPLY,  "*", startLine, startCol);
            case '/' -> new Token(TokenType.DIVIDE,    "/", startLine, startCol);
            case '(' -> new Token(TokenType.LPAREN,    "(", startLine, startCol);
            case ')' -> new Token(TokenType.RPAREN,    ")", startLine, startCol);
            case '{' -> new Token(TokenType.LBRACE,    "{", startLine, startCol);
            case '}' -> new Token(TokenType.RBRACE,    "}", startLine, startCol);
            case ';' -> new Token(TokenType.SEMICOLON, ";", startLine, startCol);
            case ',' -> new Token(TokenType.COMMA,     ",", startLine, startCol);
            case '=' -> {
                if (pos < source.length() && peek() == '=') {
                    consume();
                    yield new Token(TokenType.EQUAL, "==", startLine, startCol);
                }
                yield new Token(TokenType.ASSIGN, "=", startLine, startCol);
            }
            case '!' -> {
                if (pos < source.length() && peek() == '=') {
                    consume();
                    yield new Token(TokenType.NOT_EQUAL, "!=", startLine, startCol);
                }
                yield new Token(TokenType.ERROR,
                        "Unknown character: !", startLine, startCol);
            }
            case '<' -> {
                if (pos < source.length() && peek() == '=') {
                    consume();
                    yield new Token(TokenType.LESS_EQUAL, "<=", startLine, startCol);
                }
                yield new Token(TokenType.LESS, "<", startLine, startCol);
            }
            case '>' -> {
                if (pos < source.length() && peek() == '=') {
                    consume();
                    yield new Token(TokenType.GREATER_EQUAL, ">=", startLine, startCol);
                }
                yield new Token(TokenType.GREATER, ">", startLine, startCol);
            }
            default -> new Token(TokenType.ERROR,
                    "Unknown character: " + c, startLine, startCol);
        };
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isIdentStart(char c) {
        return Character.isLetter(c) && (c < 128 || UKRAINIAN_ALLOWED.contains(c));
    }

    private void skipWhitespace() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
                column++;
            } else if (c == '\n') {
                pos++;
                line++;
                column = 1;
            } else {
                break;
            }
        }
    }

    private char peek() {
        return source.charAt(pos);
    }

    private char peekNext() {
        return (pos + 1 < source.length()) ? source.charAt(pos + 1) : '\0';
    }

    private char consume() {
        char c = source.charAt(pos++);
        if (c == '\n') { line++; column = 1; }
        else            { column++; }
        return c;
    }
}
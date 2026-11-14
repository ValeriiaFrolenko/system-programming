package lexer;

public record Token(TokenType type, String text, int line, int column, int code) {

    public Token(TokenType type, String text, int line, int column) {
        this(type, text, line, column, -1);
    }

    @Override
    public String toString() {
        return type + "(\"" + text + "\")@" + line + ":" + column
                + (code >= 0 ? "#" + code : "");
    }
}
package lexer;

public class LexException extends RuntimeException {
    private final int line;
    private final int column;

    public LexException(int line, int column, String message) {
        super("Лексична помилка на рядку " + line + ", стовпці " + column + ": " + message);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
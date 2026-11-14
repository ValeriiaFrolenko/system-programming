package lexer;

/**
 * Помилка лексичного аналізу.
 *
 * <p>Виникає, коли {@link Lexer} натрапляє на символ, якого немає в
 * жодному правилі (наприклад, {@code @} поза коментарем), або на слово,
 * що не починається з жодної відомої основи і не є латиницею.
 *
 * <p>Містить {@link #getLine() рядок} і {@link #getColumn() стовпець} —
 * щоб у повідомленні можна було показати точне місце.
 */
public class LexException extends RuntimeException {
    private final int line;
    private final int column;

    public LexException(int line, int column, String message) {
        super("Лексична помилка на рядку " + line + ", стовпці " + column + ": " + message);
        this.line = line;
        this.column = column;
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
}

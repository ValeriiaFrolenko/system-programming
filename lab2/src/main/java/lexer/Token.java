package lexer;

/**
 * Токен — атомарна одиниця лексичного аналізу.
 *
 * <p>Містить:
 * <ul>
 *   <li>{@link #type() тип} — що це за токен;</li>
 *   <li>{@link #text() текст} — як він виглядав у вихідному файлі
 *       (для повідомлень про помилки та для {@link parser.TreeFolder}, який
 *       витягує з токена {@code IDENTIFIER} ім'я точки);</li>
 *   <li>{@link #line() рядок} і {@link #column() стовпець} — позиція в
 *       джерелі для діагностики;</li>
 *   <li>{@link #code() код} — стабільний цілочисельний код із
 *       {@link SymbolTable}, лише для {@code IDENTIFIER}, інакше {@code -1}.</li>
 * </ul>
 */
public record Token(TokenType type, String text, int line, int column, int code) {

    /** Зручний конструктор для токенів без коду (усе, крім {@code IDENTIFIER}). */
    public Token(TokenType type, String text, int line, int column) {
        this(type, text, line, column, -1);
    }

    @Override
    public String toString() {
        return type + "(\"" + text + "\")@" + line + ":" + column
                + (code >= 0 ? "#" + code : "");
    }
}

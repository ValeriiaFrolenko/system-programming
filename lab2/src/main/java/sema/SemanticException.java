package sema;

/**
 * Помилка семантичного аналізу.
 *
 * <p>Виникає, коли програма синтаксично коректна, але геометрично
 * безглузда: точка не оголошена, дві точки мають однакові координати,
 * трикутник вироджений тощо. На відміну від
 * {@link lexer.LexException} і {@link parser.ParseException}, тут немає
 * позиції в тексті — семантика працює вже з абстрактними іменами й
 * координатами, а не з токенами.
 */
public class SemanticException extends RuntimeException {
    public SemanticException(String message) {
        super("Семантична помилка: " + message);
    }
}

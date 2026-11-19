package grammar;

/**
 * Сигналізує, що граматика сформована неправильно або не задовольняє
 * умову LL(1), потрібну табличному парсеру.
 */
public class GrammarException extends RuntimeException {

    public GrammarException(String message) {
        super(message);
    }
}

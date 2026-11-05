package sema;

public class SemanticException extends RuntimeException {
    public SemanticException(String message) {
        super("Семантична помилка: " + message);
    }
}
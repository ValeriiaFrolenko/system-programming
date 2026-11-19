package grammar;

import lexer.TokenType;

/**
 * Термінальний символ граматики.
 *
 * <p>Обгортає {@link TokenType}, який породжує лексер, тож граматика
 * записується безпосередньо в термінах потоку токенів.
 *
 * @param type тип токена, якому відповідає цей термінал
 */
public record Terminal(TokenType type) implements Symbol {

    @Override
    public String toString() {
        return type.name();
    }
}

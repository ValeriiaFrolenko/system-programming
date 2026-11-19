package grammar;

import java.util.Objects;

/**
 * Нетермінальний символ граматики.
 *
 * <p>Кожен нетермінал має зрозумілу назву й числовий код. Саме код несе
 * дерево синтаксичного розбору і показує принтер дерева. Коди й назви
 * мають бути унікальними в межах однієї {@link Grammar}.
 *
 * @param code числовий код нетермінала
 * @param name назва нетермінала, не порожня
 */
public record NonTerminal(int code, String name) implements Symbol {

    public NonTerminal {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Non-terminal name must not be blank");
        }
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}

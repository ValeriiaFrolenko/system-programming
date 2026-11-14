package lexer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Таблиця ідентифікаторів.
 *
 * <p>Кожному унікальному імені (рядку) присвоює стабільний цілочисельний
 * код, який не змінюється протягом обробки всього тексту. Коди зберігаються
 * прямо в токенах {@code IDENTIFIER} (поле {@link Token#code()}) і можуть
 * використовуватися далі — наприклад, для стислого представлення імен у
 * проміжних структурах.
 *
 * <p>Порядок кодів — порядок першої появи імені (завдяки
 * {@link LinkedHashMap}); це робить кодування детермінованим для однакових
 * входів.
 */
public class SymbolTable {
    private final Map<String, Integer> nameToCode = new LinkedHashMap<>();

    /** Повертає код для імені, заводячи новий, якщо імені ще немає. */
    public int codeFor(String name) {
        return nameToCode.computeIfAbsent(name, _ -> nameToCode.size());
    }

    /** Чи вже зареєстроване ім'я. */
    public boolean contains(String name) {
        return nameToCode.containsKey(name);
    }

    /** Незмінний знімок усіх пар «ім'я → код» (для тестів і діагностики). */
    public Map<String, Integer> entries() {
        return Map.copyOf(nameToCode);
    }
}

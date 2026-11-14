package lexer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Таблиця ключових слів мови.
 *
 * <p>Два набори:
 * <ul>
 *   <li>{@link #EXACT} — короткі слова ({@code В}, {@code ДО}, {@code ПО},
 *       {@code ВІД}, {@code ЧЕРЕЗ}), які небезпечно матчити за основою
 *       (основа {@code В} збіглася б із {@code ВЕРШИНАМ});</li>
 *   <li>{@link #STEMS} — основи довших слів, що приймають будь-які
 *       відмінкові закінчення ({@code ТОЧК} → {@code ТОЧКА}, {@code ТОЧКИ}).</li>
 * </ul>
 *
 * <p>{@link #STEMS} впорядкована ({@link LinkedHashMap}) — для
 * детермінованої ітерації та передбачуваних повідомлень про помилки.
 * Наразі жодна зареєстрована основа <b>не є префіксом іншої</b>.
 * Якщо в майбутньому додасте основу, що є префіксом іншої, порядок
 * вставки стане важливим: специфічніша повинна йти першою.
 */
public final class KeywordTable {
    private static final Map<String, TokenType> EXACT = new HashMap<>();
    private static final Map<String, TokenType> STEMS = new LinkedHashMap<>();

    static {
        EXACT.put("В", TokenType.AT);
        EXACT.put("ДО", TokenType.TO);
        EXACT.put("ПО", TokenType.BY);
        EXACT.put("ВІД", TokenType.FROM);
        EXACT.put("ЧЕРЕЗ", TokenType.THROUGH);

        STEMS.put("ПЕРПЕНДИКУЛЯР", TokenType.PERPENDICULAR);
        STEMS.put("ТРИКУТНИК", TokenType.TRIANGLE);
        STEMS.put("ПОБУДУВА", TokenType.BUILD);
        STEMS.put("ПРОВЕСТ", TokenType.DRAW);
        STEMS.put("ВЕРШИН", TokenType.VERTEX);
        STEMS.put("ТРЬОМ", TokenType.THREE);
        STEMS.put("ТОЧЦ", TokenType.POINT_LOC);
        STEMS.put("ТОЧК", TokenType.POINT);
        STEMS.put("ПРЯМ", TokenType.LINE);
        STEMS.put("ВІДРІЗ", TokenType.SEGMENT);
    }

    private KeywordTable() {}

    /**
     * Класифікує слово, вже приведене до верхнього регістру.
     *
     * @return тип токена, якщо слово — ключове, інакше {@code null}
     *         (сигнал {@link Lexer}, що перед ним, можливо, ідентифікатор)
     */
    public static TokenType classify(String upperWord) {
        TokenType exact = EXACT.get(upperWord);
        if (exact != null) return exact;

        for (Map.Entry<String, TokenType> entry : STEMS.entrySet()) {
            if (upperWord.startsWith(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}
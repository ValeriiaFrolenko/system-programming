package sema;

import java.util.HashSet;
import java.util.Set;

/**
 * Перевірки, специфічні для трикутника.
 *
 * <p>Дві умови, які не стосуються інших примітивів:
 * <ul>
 *   <li>усі три вершини — різні точки (порівняння за іменем);</li>
 *   <li>площа трикутника не нульова (інакше він вироджується у
 *       відрізок і {@link render.CodeGenerator} намалював би замість
 *       полігону лінію).</li>
 * </ul>
 *
 * <p>Винесено з {@link SemanticAnalyzer}, щоб алгоритм площі можна було
 * протестувати окремо і щоб основний аналізатор залишався коротким.
 */
public final class TriangleValidator {

    /** Допуск для порівняння площі з нулем (захист від похибок округлення). */
    private static final double AREA_EPSILON = 1e-9;

    private TriangleValidator() {}

    /**
     * Кидає {@link SemanticException}, якщо трикутник некоректний.
     * Нічого не повертає — успіх означає «можна додавати в модель».
     */
    public static void validate(GeometryModel.Point a, GeometryModel.Point b, GeometryModel.Point c) {
        Set<String> uniqueNames = new HashSet<>();
        uniqueNames.add(a.name());
        uniqueNames.add(b.name());
        uniqueNames.add(c.name());
        if (uniqueNames.size() < 3) {
            throw new SemanticException("Трикутник не може мати повторювані вершини");
        }

        double area = Math.abs(
                (b.x() - a.x()) * (c.y() - a.y())
              - (c.x() - a.x()) * (b.y() - a.y())) / 2.0;
        if (area < AREA_EPSILON) {
            throw new SemanticException("Трикутник вироджений");
        }
    }
}

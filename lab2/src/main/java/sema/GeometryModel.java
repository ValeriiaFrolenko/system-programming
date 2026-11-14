package sema;

import java.util.List;
import java.util.Map;

/**
 * Математична модель геометричної побудови — результат семантичного
 * аналізу.
 *
 * <p>Містить лише валідовані сутності: точки з координатами, прямі,
 * перпендикуляри, трикутники, відрізки. Усі посилання на точки вже
 * розв'язані (замість імен — об'єкти {@link Point}), тож споживачам
 * моделі (наприклад, {@link render.CodeGenerator}) не треба нічого
 * шукати самостійно.
 *
 * <p>Колекції незмінні (defensive copy у канонічному конструкторі).
 * Порядок збережено той самий, у якому об'єкти з'являлися у вихідній
 * програмі — це впливає на порядок малювання в {@link render.Renderer}.
 */
public record GeometryModel(
        Map<String, Point> points,
        List<Line> lines,
        List<Perpendicular> perpendiculars,
        List<Triangle> triangles,
        List<Segment> segments
) {
    public GeometryModel {
        points = Map.copyOf(points);
        lines = List.copyOf(lines);
        perpendiculars = List.copyOf(perpendiculars);
        triangles = List.copyOf(triangles);
        segments = List.copyOf(segments);
    }

    /** Точка з ім'ям і координатами. */
    public record Point(String name, double x, double y) {}

    /** Пряма через дві точки (нескінченна, не відрізок). */
    public record Line(Point a, Point b) {}

    /** Перпендикуляр до прямої {@code (baseA, baseB)} через точку {@code target}. */
    public record Perpendicular(Point baseA, Point baseB, Point target) {}

    /** Трикутник за трьома вершинами. */
    public record Triangle(Point a, Point b, Point c) {}

    /** Відрізок між двома точками. */
    public record Segment(Point start, Point end) {}
}

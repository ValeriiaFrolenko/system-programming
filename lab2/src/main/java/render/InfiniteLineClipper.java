package render;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Обрізання нескінченної прямої по прямокутнику полотна.
 *
 * <p>Чиста геометрія, без {@link java.awt.Graphics2D}: приймає пряму в
 * піксельних координатах і повертає точки перетину з чотирма межами
 * полотна. Використовується з {@link PrimitivePainter}, але може бути
 * протестований ізольовано.
 *
 * <p>Пряма задається точкою {@code (sx0, sy0)} і напрямком
 * {@code (sdx, sdy)}. Для кожного ребра полотна розв'язується
 * параметричне рівняння й перевіряється, чи точка перетину лежить у
 * межах відповідної сторони.
 */
public class InfiniteLineClipper {

    /**
     * Повертає 0, 1 або 2 точки перетину прямої з межами полотна.
     * Порожній список — якщо пряма не перетинає полотно (вироджений
     * напрямок або проходить повз).
     */
    public List<Point2D.Double> clip(double sx0, double sy0, double sdx, double sdy, int width, int height) {
        List<Point2D.Double> intersections = new ArrayList<>();
        if (sdx == 0 && sdy == 0) return intersections;

        if (sdx != 0) {
            double t1 = -sx0 / sdx;
            double y1 = sy0 + t1 * sdy;
            if (y1 >= 0 && y1 <= height) intersections.add(new Point2D.Double(0, y1));

            double t2 = (width - sx0) / sdx;
            double y2 = sy0 + t2 * sdy;
            if (y2 >= 0 && y2 <= height) intersections.add(new Point2D.Double(width, y2));
        }

        if (sdy != 0) {
            double t3 = -sy0 / sdy;
            double x3 = sx0 + t3 * sdx;
            if (x3 >= 0 && x3 <= width) intersections.add(new Point2D.Double(x3, 0));

            double t4 = (height - sy0) / sdy;
            double x4 = sx0 + t4 * sdx;
            if (x4 >= 0 && x4 <= width) intersections.add(new Point2D.Double(x4, height));
        }

        return intersections;
    }
}

package render;

import java.util.List;

/**
 * Команди графічного пакета — проміжне представлення між семантичною
 * моделлю ({@link sema.GeometryModel}) і виконанням малювання.
 *
 * Кожна команда описує одну дію малювання в математичних координатах
 * моделі (не в пікселях вікна) і не містить жодної інформації про
 * геометричну семантику (трикутник, перпендикуляр тощо) — лише
 * примітив, який потрібно намалювати.
 */
public sealed interface GraphicsCommand {

    /** Намалювати точку з підписом імені поруч. */
    record DrawPoint(double x, double y, String label) implements GraphicsCommand {}

    /** Намалювати відрізок між двома точками. */
    record DrawSegment(double x1, double y1, double x2, double y2) implements GraphicsCommand {}

    /** Намалювати нескінченну пряму, що проходить через (x0, y0) у напрямку (dx, dy). */
    record DrawInfiniteLine(double x0, double y0, double dx, double dy) implements GraphicsCommand {}

    /** Намалювати замкнений многокутник по списку вершин (у порядку обходу). */
    record DrawPolygon(List<double[]> vertices) implements GraphicsCommand {}
}
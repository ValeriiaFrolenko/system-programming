package render;

import java.util.List;

/**
 * Команди графічного пакета — проміжне представлення між семантичною
 * моделлю ({@link sema.GeometryModel}) і виконанням малювання
 * ({@link Renderer}).
 *
 * <p>Кожна команда описує одну дію малювання <b>в математичних
 * координатах моделі</b> (не в пікселях вікна) і не містить жодної
 * інформації про геометричну семантику (трикутник, перпендикуляр
 * тощо) — лише примітив, який треба намалювати. Завдяки цьому
 * {@link CodeGenerator} нічого не знає про масштабування й обрізання,
 * а {@link Renderer} нічого не знає про трикутники й перпендикуляри.
 *
 * <p>Рівно чотири примітиви:
 * <ul>
 *   <li>{@link DrawPoint} — точка з підписом;</li>
 *   <li>{@link DrawSegment} — скінченний відрізок;</li>
 *   <li>{@link DrawInfiniteLine} — нескінченна пряма, задана точкою і
 *       напрямком;</li>
 *   <li>{@link DrawPolygon} — замкнений многокутник.</li>
 * </ul>
 */
public sealed interface GraphicsCommand {

    /** Намалювати точку з підписом імені поруч. */
    record DrawPoint(double x, double y, String label) implements GraphicsCommand {}

    /** Намалювати відрізок між двома точками. */
    record DrawSegment(double x1, double y1, double x2, double y2) implements GraphicsCommand {}

    /** Намалювати нескінченну пряму, що проходить через {@code (x0, y0)} у напрямку {@code (dx, dy)}. */
    record DrawInfiniteLine(double x0, double y0, double dx, double dy) implements GraphicsCommand {}

    /** Намалювати замкнений многокутник по списку вершин (у порядку обходу). */
    record DrawPolygon(List<double[]> vertices) implements GraphicsCommand {}
}

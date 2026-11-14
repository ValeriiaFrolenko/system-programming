package render;

import java.util.List;

/**
 * Перетворення математичних координат у піксельні.
 *
 * <p>Обчислює bounding box зі списку {@link GraphicsCommand} (а не з
 * {@link sema.GeometryModel}), тому лишається частиною виконавця
 * ({@link Renderer}) — {@link CodeGenerator} про нього нічого не знає.
 *
 * <p>Що робить:
 * <ul>
 *   <li>знаходить мінімальний прямокутник, що містить усі точки,
 *       кінці відрізків і вершини полігонів, а також точки проходження
 *       нескінченних прямих;</li>
 *   <li>підбирає єдиний коефіцієнт масштабу {@code scale}, щоб фігура
 *       вмістилася в полотно з відступами {@code padding} з усіх боків;</li>
 *   <li>інвертує вісь Y (у математиці Y росте вгору, у Java2D — вниз);</li>
 *   <li>центрує результат у доступній області.</li>
 * </ul>
 *
 * <p>Порожній список команд дає scale=1 і стандартне вирівнювання —
 * щоб не ділити на нуль і не падати на порожньому вході.
 */
public class Viewport {
    private final int width;
    private final int height;
    private final double scale;
    private final double offsetX;
    private final double offsetY;

    /**
     * Будує viewport за списком команд.
     *
     * @param commands команди, з яких обчислюється bounding box
     * @param width    ширина полотна в пікселях
     * @param height   висота полотна в пікселях
     * @param padding  відступ від краю полотна в пікселях
     */
    public Viewport(List<GraphicsCommand> commands, int width, int height, int padding) {
        this.width = width;
        this.height = height;

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        boolean any = false;

        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawPoint p) {
                minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
                any = true;
            } else if (cmd instanceof GraphicsCommand.DrawSegment(double x1, double y1, double x2, double y2)) {
                minX = Math.min(minX, Math.min(x1, x2));
                maxX = Math.max(maxX, Math.max(x1, x2));
                minY = Math.min(minY, Math.min(y1, y2));
                maxY = Math.max(maxY, Math.max(y1, y2));
                any = true;
            } else if (cmd instanceof GraphicsCommand.DrawPolygon(List<double[]> vertices)) {
                for (double[] v : vertices) {
                    minX = Math.min(minX, v[0]); maxX = Math.max(maxX, v[0]);
                    minY = Math.min(minY, v[1]); maxY = Math.max(maxY, v[1]);
                }
                any = true;
            } else if (cmd instanceof GraphicsCommand.DrawInfiniteLine l) {
                // Точка проходження нескінченної прямої теж бере участь
                // у визначенні масштабу — щоб пряма потрапила в кадр
                // разом з рештою об'єктів.
                minX = Math.min(minX, l.x0()); maxX = Math.max(maxX, l.x0());
                minY = Math.min(minY, l.y0()); maxY = Math.max(maxY, l.y0());
                any = true;
            }
        }

        if (!any) {
            this.scale = 1.0;
            this.offsetX = padding;
            this.offsetY = height - padding;
            return;
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        if (rangeX == 0) rangeX = 1;
        if (rangeY == 0) rangeY = 1;

        double availableWidth = width - 2.0 * padding;
        double availableHeight = height - 2.0 * padding;

        this.scale = Math.min(availableWidth / rangeX, availableHeight / rangeY);
        this.offsetX = padding + (availableWidth - rangeX * scale) / 2.0 - minX * scale;
        this.offsetY = height - padding - (availableHeight - rangeY * scale) / 2.0 + minY * scale;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getScale() { return scale; }

    /** Математичне X → піксельне X (дробове значення). */
    public double toScreenXDouble(double x) {
        return x * scale + offsetX;
    }

    /** Математичне Y → піксельне Y (дробове значення; вісь інвертована). */
    public double toScreenYDouble(double y) {
        return -y * scale + offsetY;
    }

    /** Те саме, що {@link #toScreenXDouble}, з округленням до int. */
    public int toScreenX(double x) {
        return (int) Math.round(toScreenXDouble(x));
    }

    /** Те саме, що {@link #toScreenYDouble}, з округленням до int. */
    public int toScreenY(double y) {
        return (int) Math.round(toScreenYDouble(y));
    }
}

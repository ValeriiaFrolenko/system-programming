package render;

import java.util.List;

/**
 * Відповідає за масштабування математичних координат у піксельні.
 * Межі (bounding box) обчислюються з самих команд малювання, а не з
 * геометричної моделі — Viewport лишається частиною виконавця
 * (Renderer), генератор команд про нього нічого не знає.
 */
public class Viewport {
    private final int width;
    private final int height;
    private final double scale;
    private final double offsetX;
    private final double offsetY;

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

    public double toScreenXDouble(double x) {
        return x * scale + offsetX;
    }

    public double toScreenYDouble(double y) {
        return -y * scale + offsetY;
    }

    public int toScreenX(double x) {
        return (int) Math.round(toScreenXDouble(x));
    }

    public int toScreenY(double y) {
        return (int) Math.round(toScreenYDouble(y));
    }
}
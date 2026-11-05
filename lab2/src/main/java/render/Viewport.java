package render;

import sema.GeometryModel;

import java.util.Collection;

public class Viewport {
    private final int width;
    private final int height;
    private final double scale;
    private final double offsetX;
    private final double offsetY;

    public Viewport(GeometryModel model, int width, int height, int padding) {
        this.width = width;
        this.height = height;

        Collection<GeometryModel.Point> points = model.points().values();
        if (points.isEmpty()) {
            this.scale = 1.0;
            this.offsetX = padding;
            this.offsetY = height - padding;
            return;
        }

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (GeometryModel.Point p : points) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
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
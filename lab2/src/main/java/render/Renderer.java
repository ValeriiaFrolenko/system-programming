package render;

import sema.GeometryModel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    private static final Color COLOR_POINT = Color.BLACK;
    private static final Color COLOR_LINE = new Color(0, 100, 200);
    private static final Color COLOR_PERP = new Color(220, 50, 50);
    private static final Color COLOR_TRIANGLE = new Color(50, 180, 50);
    private static final Color COLOR_SEGMENT = new Color(255, 140, 0);

    public void render(GeometryModel model, String outputPath) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setStroke(new BasicStroke(2f));

        Viewport vp = new Viewport(model, WIDTH, HEIGHT, PADDING);

        g.setColor(COLOR_TRIANGLE);
        for (GeometryModel.Triangle t : model.triangles()) {
            int[] xs = {vp.toScreenX(t.a().x()), vp.toScreenX(t.b().x()), vp.toScreenX(t.c().x())};
            int[] ys = {vp.toScreenY(t.a().y()), vp.toScreenY(t.b().y()), vp.toScreenY(t.c().y())};
            g.drawPolygon(xs, ys, 3);
        }

        g.setColor(COLOR_SEGMENT);
        for (GeometryModel.Segment s : model.segments()) {
            g.drawLine(vp.toScreenX(s.start().x()), vp.toScreenY(s.start().y()), vp.toScreenX(s.end().x()), vp.toScreenY(s.end().y()));
        }

        g.setColor(COLOR_LINE);
        for (GeometryModel.Line l : model.lines()) {
            drawInfiniteLine(g, vp, l.a().x(), l.a().y(), l.b().x() - l.a().x(), l.b().y() - l.a().y());
        }

        g.setColor(COLOR_PERP);
        for (GeometryModel.Perpendicular p : model.perpendiculars()) {
            double dx = p.baseB().x() - p.baseA().x();
            double dy = p.baseB().y() - p.baseA().y();
            drawInfiniteLine(g, vp, p.target().x(), p.target().y(), -dy, dx);
        }

        g.setColor(COLOR_POINT);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        for (GeometryModel.Point p : model.points().values()) {
            int x = vp.toScreenX(p.x());
            int y = vp.toScreenY(p.y());
            g.fillOval(x - 4, y - 4, 8, 8);
            g.drawString(p.name(), x + 8, y - 8);
        }

        g.dispose();
        ImageIO.write(image, "png", new File(outputPath));
    }

    private void drawInfiniteLine(Graphics2D g, Viewport vp, double x0, double y0, double dx, double dy) {
        if (dx == 0 && dy == 0) return;

        double sx0 = vp.toScreenXDouble(x0);
        double sy0 = vp.toScreenYDouble(y0);
        double sdx = dx * vp.getScale();
        double sdy = -dy * vp.getScale();

        List<Point2D.Double> intersections = new ArrayList<>();

        if (sdx != 0) {
            double t1 = -sx0 / sdx;
            double y1 = sy0 + t1 * sdy;
            if (y1 >= 0 && y1 <= vp.getHeight()) intersections.add(new Point2D.Double(0, y1));

            double t2 = (vp.getWidth() - sx0) / sdx;
            double y2 = sy0 + t2 * sdy;
            if (y2 >= 0 && y2 <= vp.getHeight()) intersections.add(new Point2D.Double(vp.getWidth(), y2));
        }

        if (sdy != 0) {
            double t3 = -sy0 / sdy;
            double x3 = sx0 + t3 * sdx;
            if (x3 >= 0 && x3 <= vp.getWidth()) intersections.add(new Point2D.Double(x3, 0));

            double t4 = (vp.getHeight() - sy0) / sdy;
            double x4 = sx0 + t4 * sdx;
            if (x4 >= 0 && x4 <= vp.getWidth()) intersections.add(new Point2D.Double(x4, vp.getHeight()));
        }

        if (intersections.size() >= 2) {
            g.draw(new Line2D.Double(intersections.get(0), intersections.get(intersections.size() - 1)));
        }
    }
}

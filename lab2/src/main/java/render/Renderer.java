package render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Виконавець команд графічного пакета. Не знає нічого про геометричну
 * семантику (трикутники, перпендикуляри, прямі через точки тощо) —
 * лише вміє намалювати чотири примітиви з {@link GraphicsCommand}:
 * точку, відрізок, нескінченну пряму і многокутник. Масштабування і
 * обрізання нескінченних ліній по межах вікна (Viewport) — це його
 * власна відповідальність виконавця, а не генератора команд.
 */
public class Renderer {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    private static final Color COLOR_POINT = Color.BLACK;
    private static final Color COLOR_INFINITE_LINE = new Color(0, 100, 200);
    private static final Color COLOR_POLYGON = new Color(50, 180, 50);
    private static final Color COLOR_SEGMENT = new Color(255, 140, 0);

    public void render(List<GraphicsCommand> commands, String outputPath) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setStroke(new BasicStroke(2f));

        Viewport vp = new Viewport(commands, WIDTH, HEIGHT, PADDING);

        // Порядок малювання: полігони і відрізки спершу, нескінченні
        // прямі поверх них, точки з підписами останніми — щоб підписи
        // не ховались під лініями.
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawPolygon p) drawPolygon(g, vp, p);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawSegment s) drawSegment(g, vp, s);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawInfiniteLine l) drawInfiniteLine(g, vp, l);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawPoint p) drawPoint(g, vp, p);
        }

        g.dispose();
        ImageIO.write(image, "png", new File(outputPath));
    }

    private void drawPolygon(Graphics2D g, Viewport vp, GraphicsCommand.DrawPolygon cmd) {
        List<double[]> vertices = cmd.vertices();
        int n = vertices.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = vp.toScreenX(vertices.get(i)[0]);
            ys[i] = vp.toScreenY(vertices.get(i)[1]);
        }
        g.setColor(COLOR_POLYGON);
        g.drawPolygon(xs, ys, n);
    }

    private void drawSegment(Graphics2D g, Viewport vp, GraphicsCommand.DrawSegment cmd) {
        g.setColor(COLOR_SEGMENT);
        g.drawLine(
                vp.toScreenX(cmd.x1()), vp.toScreenY(cmd.y1()),
                vp.toScreenX(cmd.x2()), vp.toScreenY(cmd.y2()));
    }

    private void drawPoint(Graphics2D g, Viewport vp, GraphicsCommand.DrawPoint cmd) {
        int x = vp.toScreenX(cmd.x());
        int y = vp.toScreenY(cmd.y());
        g.setColor(COLOR_POINT);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.fillOval(x - 4, y - 4, 8, 8);
        g.drawString(cmd.label(), x + 8, y - 8);
    }

    private void drawInfiniteLine(Graphics2D g, Viewport vp, GraphicsCommand.DrawInfiniteLine cmd) {
        double dx = cmd.dx();
        double dy = cmd.dy();
        if (dx == 0 && dy == 0) return;

        double sx0 = vp.toScreenXDouble(cmd.x0());
        double sy0 = vp.toScreenYDouble(cmd.y0());
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

        g.setColor(COLOR_INFINITE_LINE);
        if (intersections.size() >= 2) {
            g.draw(new Line2D.Double(intersections.getFirst(), intersections.getLast()));
        }
    }
}
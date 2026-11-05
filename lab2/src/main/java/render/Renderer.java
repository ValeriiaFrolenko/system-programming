package render;

import sema.GeometryModel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Renderer {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    private static final Color COLOR_POINT = Color.BLACK;
    private static final Color COLOR_LINE = new Color(0, 100, 200);
    private static final Color COLOR_PERP = new Color(220, 50, 50);
    private static final Color COLOR_TRIANGLE = new Color(50, 180, 50);

    public void render(GeometryModel model, String outputPath) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Viewport vp = new Viewport(model, WIDTH, HEIGHT, PADDING);

        g.setColor(COLOR_TRIANGLE);
        for (GeometryModel.Triangle t : model.triangles()) {
            int[] xs = {vp.toScreenX(t.a().x()), vp.toScreenX(t.b().x()), vp.toScreenX(t.c().x())};
            int[] ys = {vp.toScreenY(t.a().y()), vp.toScreenY(t.b().y()), vp.toScreenY(t.c().y())};
            g.drawPolygon(xs, ys, 3);
        }

        g.setColor(COLOR_LINE);
        g.setStroke(new BasicStroke(2f));
        for (GeometryModel.Segment s : model.lines()) {
            drawInfiniteLine(g, vp, s);
        }

        g.setColor(COLOR_PERP);
        for (GeometryModel.Segment p : model.perpendiculars()) {
            drawInfiniteLine(g, vp, p);
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

    private void drawInfiniteLine(Graphics2D g, Viewport vp, GeometryModel.Segment s) {
        double x1 = s.start().x(), y1 = s.start().y();
        double x2 = s.end().x(), y2 = s.end().y();
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);

        if (len == 0) return;

        double ux = dx / len, uy = dy / len;

        // Розтягуємо лінію далеко за межі полотна, щоб отримати ефект нескінченної прямої
        double big = 1e5;
        int sx1 = vp.toScreenX(x1 - ux * big);
        int sy1 = vp.toScreenY(y1 - uy * big);
        int sx2 = vp.toScreenX(x2 + ux * big);
        int sy2 = vp.toScreenY(y2 + uy * big);

        g.drawLine(sx1, sy1, sx2, sy2);
    }
}
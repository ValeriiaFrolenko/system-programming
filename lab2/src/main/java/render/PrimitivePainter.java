package render;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Малює чотири примітиви з {@link GraphicsCommand} у заданому
 * {@link Graphics2D} з урахуванням масштабу {@link Viewport}.
 */
public class PrimitivePainter {
    private static final Color COLOR_POINT = Color.BLACK;
    private static final Color COLOR_INFINITE_LINE = new Color(0, 100, 200);
    private static final Color COLOR_POLYGON = new Color(50, 180, 50);
    private static final Color COLOR_SEGMENT = new Color(255, 140, 0);

    private final Graphics2D g;
    private final Viewport vp;
    private final InfiniteLineClipper clipper = new InfiniteLineClipper();

    public PrimitivePainter(Graphics2D g, Viewport vp) {
        this.g = g;
        this.vp = vp;
    }

    /** Малює замкнений многокутник зеленим. */
    public void drawPolygon(GraphicsCommand.DrawPolygon cmd) {
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

    /** Малює скінченний відрізок оранжевим. */
    public void drawSegment(GraphicsCommand.DrawSegment cmd) {
        g.setColor(COLOR_SEGMENT);
        g.drawLine(
                vp.toScreenX(cmd.x1()), vp.toScreenY(cmd.y1()),
                vp.toScreenX(cmd.x2()), vp.toScreenY(cmd.y2()));
    }

    /** Малює точку чорним кружечком із підписом імені. */
    public void drawPoint(GraphicsCommand.DrawPoint cmd) {
        int x = vp.toScreenX(cmd.x());
        int y = vp.toScreenY(cmd.y());
        g.setColor(COLOR_POINT);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.fillOval(x - 4, y - 4, 8, 8);
        g.drawString(cmd.label(), x + 8, y - 8);
    }

    /** Малює нескінченну пряму синім, обрізаючи її по межах полотна. */
    public void drawInfiniteLine(GraphicsCommand.DrawInfiniteLine cmd) {
        List<Point2D.Double> endpoints = clipper.clip(
                vp.toScreenXDouble(cmd.x0()),
                vp.toScreenYDouble(cmd.y0()),
                cmd.dx() * vp.getScale(),
                -cmd.dy() * vp.getScale(),
                vp.getWidth(),
                vp.getHeight());

        if (endpoints.size() < 2) return;

        g.setColor(COLOR_INFINITE_LINE);
        g.draw(new Line2D.Double(endpoints.get(0), endpoints.get(1)));
    }
}

package render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Оркестратор малювання: створює полотно, проганяє команди в
 * правильному порядку, повертає готове зображення.
 *
 * <p>Не знає, як виглядає жоден примітив — усе делегує
 * {@link PrimitivePainter}. Його єдина робота:
 * <ol>
 *   <li>створити {@link BufferedImage} потрібного розміру й залити білим;</li>
 *   <li>побудувати {@link Viewport} за списком команд;</li>
 *   <li>викликати {@code draw*} у фіксованому порядку: полігони →
 *       відрізки → нескінченні прямі → точки (щоб дрібні елементи були
 *       зверху).</li>
 * </ol>
 */
public class Renderer {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    /** Виконує всі команди і повертає готове зображення. */
    public BufferedImage render(List<GraphicsCommand> commands) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setStroke(new BasicStroke(2f));

        Viewport vp = new Viewport(commands, WIDTH, HEIGHT, PADDING);
        PrimitivePainter painter = new PrimitivePainter(g, vp);

        // Порядок малювання фіксований: полігони → відрізки →
        // нескінченні прямі → точки. Точки з підписами — останніми,
        // щоб їх не ховали лінії.
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawPolygon p) painter.drawPolygon(p);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawSegment s) painter.drawSegment(s);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawInfiniteLine l) painter.drawInfiniteLine(l);
        }
        for (GraphicsCommand cmd : commands) {
            if (cmd instanceof GraphicsCommand.DrawPoint p) painter.drawPoint(p);
        }

        g.dispose();
        return image;
    }
}

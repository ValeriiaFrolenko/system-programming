package render;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewportTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    @Test
    void emptyCommandListUsesDefaultOffsetAndUnitScale() {
        Viewport vp = new Viewport(List.of(), WIDTH, HEIGHT, PADDING);
        assertEquals(1.0, vp.getScale());
        assertEquals(PADDING, vp.toScreenX(0));
        assertEquals(HEIGHT - PADDING, vp.toScreenY(0));
    }

    @Test
    void boundsComputedFromDrawPoint() {
        List<GraphicsCommand> commands = List.of(
                new GraphicsCommand.DrawPoint(0, 0, "A"),
                new GraphicsCommand.DrawPoint(10, 10, "B"));
        Viewport vp = new Viewport(commands, WIDTH, HEIGHT, PADDING);

        // обидві точки повинні потрапляти в межі полотна з відступом
        int xA = vp.toScreenX(0);
        int xB = vp.toScreenX(10);
        assertTrue(xA >= PADDING && xA <= WIDTH - PADDING);
        assertTrue(xB >= PADDING && xB <= WIDTH - PADDING);
    }

    @Test
    void boundsComputedFromDrawSegmentEndpoints() {
        List<GraphicsCommand> commands = List.of(
                new GraphicsCommand.DrawSegment(-5, -5, 5, 5));
        Viewport vp = new Viewport(commands, WIDTH, HEIGHT, PADDING);

        int xStart = vp.toScreenX(-5);
        int xEnd = vp.toScreenX(5);
        assertTrue(xStart >= PADDING);
        assertTrue(xEnd <= WIDTH - PADDING);
    }

    @Test
    void boundsComputedFromDrawPolygonVertices() {
        List<GraphicsCommand> commands = List.of(
                new GraphicsCommand.DrawPolygon(List.of(
                        new double[]{0, 0}, new double[]{100, 0}, new double[]{0, 100})));
        Viewport vp = new Viewport(commands, WIDTH, HEIGHT, PADDING);

        assertTrue(vp.getScale() > 0);
        int x = vp.toScreenX(100);
        assertTrue(x <= WIDTH - PADDING);
    }

    @Test
    void infiniteLinePassThroughPointContributesToBounds() {
        // Сам по собі DrawInfiniteLine не має кінцевих точок, але точка
        // проходження (x0, y0) все одно має впливати на масштаб — інакше
        // пряма могла б "не влізти" в кадр разом з рештою об'єктів.
        List<GraphicsCommand> withLineOnly = List.of(
                new GraphicsCommand.DrawInfiniteLine(50, 50, 1, 0));
        List<GraphicsCommand> withPointOnSameSpot = List.of(
                new GraphicsCommand.DrawPoint(50, 50, "P"));

        Viewport vpLine = new Viewport(withLineOnly, WIDTH, HEIGHT, PADDING);
        Viewport vpPoint = new Viewport(withPointOnSameSpot, WIDTH, HEIGHT, PADDING);

        // одна точка (50,50) як єдина межа -> rangeX=rangeY=1 (мінімальний
        // діапазон за замовчуванням), поведінка має бути ідентична
        assertEquals(vpPoint.getScale(), vpLine.getScale());
        assertEquals(vpPoint.toScreenX(50), vpLine.toScreenX(50));
        assertEquals(vpPoint.toScreenY(50), vpLine.toScreenY(50));
    }

    @Test
    void degenerateRangeDoesNotDivideByZero() {
        // Усі команди в одній точці -> rangeX = rangeY = 0, Viewport має
        // підставити мінімальний діапазон (1) замість ділення на нуль.
        List<GraphicsCommand> commands = List.of(new GraphicsCommand.DrawPoint(3, 3, "A"));
        assertDoesNotThrow(() -> new Viewport(commands, WIDTH, HEIGHT, PADDING));
    }
}
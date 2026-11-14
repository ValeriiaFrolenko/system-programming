package render;

import org.junit.jupiter.api.Test;
import sema.GeometryModel;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {

    private final CodeGenerator generator = new CodeGenerator();

    private static GeometryModel.Point point(String name, double x, double y) {
        return new GeometryModel.Point(name, x, y);
    }

    @Test
    void generatesDrawPointForEachPoint() {
        GeometryModel.Point a = point("A", 1, 2);
        GeometryModel model = new GeometryModel(Map.of("A", a), List.of(), List.of(), List.of(), List.of());

        List<GraphicsCommand> commands = generator.generate(model);

        assertEquals(1, commands.size());
        GraphicsCommand.DrawPoint cmd = (GraphicsCommand.DrawPoint) commands.getFirst();
        assertEquals(1, cmd.x());
        assertEquals(2, cmd.y());
        assertEquals("A", cmd.label());
    }

    @Test
    void generatesDrawSegmentWithOriginalCoordinates() {
        GeometryModel.Point a = point("A", 0, 0);
        GeometryModel.Point b = point("B", 5, 5);
        GeometryModel model = new GeometryModel(
                Map.of("A", a, "B", b), List.of(), List.of(), List.of(),
                List.of(new GeometryModel.Segment(a, b)));

        List<GraphicsCommand> commands = generator.generate(model);

        GraphicsCommand.DrawSegment seg = commands.stream()
                .filter(GraphicsCommand.DrawSegment.class::isInstance)
                .map(GraphicsCommand.DrawSegment.class::cast)
                .findFirst().orElseThrow();
        assertEquals(0, seg.x1());
        assertEquals(0, seg.y1());
        assertEquals(5, seg.x2());
        assertEquals(5, seg.y2());
    }

    @Test
    void generatesDrawInfiniteLineWithDirectionVector() {
        GeometryModel.Point a = point("A", 1, 1);
        GeometryModel.Point b = point("B", 4, 1);
        GeometryModel model = new GeometryModel(
                Map.of("A", a, "B", b), List.of(new GeometryModel.Line(a, b)),
                List.of(), List.of(), List.of());

        List<GraphicsCommand> commands = generator.generate(model);

        GraphicsCommand.DrawInfiniteLine line = commands.stream()
                .filter(GraphicsCommand.DrawInfiniteLine.class::isInstance)
                .map(GraphicsCommand.DrawInfiniteLine.class::cast)
                .findFirst().orElseThrow();
        assertEquals(1, line.x0());
        assertEquals(1, line.y0());
        assertEquals(3, line.dx()); // 4 - 1
        assertEquals(0, line.dy()); // 1 - 1
    }

    @Test
    void generatesDrawInfiniteLineForPerpendicularRotatedThroughTarget() {
        GeometryModel.Point a = point("A", 0, 0);
        GeometryModel.Point b = point("B", 1, 0);
        GeometryModel.Point c = point("C", 5, 5);
        GeometryModel model = new GeometryModel(
                Map.of("A", a, "B", b, "C", c), List.of(), List.of(new GeometryModel.Perpendicular(a, b, c)),
                List.of(), List.of());

        List<GraphicsCommand> commands = generator.generate(model);

        GraphicsCommand.DrawInfiniteLine line = commands.stream()
                .filter(GraphicsCommand.DrawInfiniteLine.class::isInstance)
                .map(GraphicsCommand.DrawInfiniteLine.class::cast)
                .findFirst().orElseThrow();
        // пряма проходить через цільову точку C
        assertEquals(5, line.x0());
        assertEquals(5, line.y0());
        // Базовий вектор (1,0) повернутий на 90° -> (-dy, dx).
        // dy базового вектора тут рівно 0.0, тож -dy за IEEE 754 дає -0.0
        // (окреме бітове представлення нуля зі знаком) — коректний і
        // детермінований наслідок формули повороту, не помилка
        // CodeGenerator. assertEquals(double, double) порівнює буквально
        // (через Double.compare усередині), тому й падає на 0.0 vs -0.0,
        // хоча 0.0 == -0.0 за звичайним порівнянням. Перевіряємо
        // напряму через Double.compare, явно визнаючи -0.0 як очікуваний
        // результат, а не підлаштовуємо вхідні дані, щоб його уникнути.
        assertEquals(0, Double.compare(-0.0, line.dx()));
        assertEquals(1, line.dy());
    }

    @Test
    void generatesDrawPolygonWithVerticesInGivenOrder() {
        GeometryModel.Point a = point("A", 0, 0);
        GeometryModel.Point b = point("B", 4, 0);
        GeometryModel.Point c = point("C", 0, 3);
        GeometryModel model = new GeometryModel(
                Map.of("A", a, "B", b, "C", c), List.of(), List.of(),
                List.of(new GeometryModel.Triangle(a, b, c)), List.of());

        List<GraphicsCommand> commands = generator.generate(model);

        GraphicsCommand.DrawPolygon poly = (GraphicsCommand.DrawPolygon) commands.getFirst();
        assertEquals(3, poly.vertices().size());
        assertArrayEquals(new double[]{0, 0}, poly.vertices().get(0));
        assertArrayEquals(new double[]{4, 0}, poly.vertices().get(1));
        assertArrayEquals(new double[]{0, 3}, poly.vertices().get(2));
    }

    @Test
    void emptyModelProducesNoCommands() {
        GeometryModel model = new GeometryModel(Map.of(), List.of(), List.of(), List.of(), List.of());
        assertTrue(generator.generate(model).isEmpty());
    }
}
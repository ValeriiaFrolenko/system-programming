package render;

import sema.GeometryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Генерація команд графічного пакета — окремий прохід після
 * семантичного аналізу. Обходить {@link GeometryModel} і породжує
 * список {@link GraphicsCommand}: опис того, що треба намалювати,
 * мовою графічного пакета, у математичних координатах моделі
 * (без піксельної логіки, без масштабування, без обрізання по вікну).
 *
 * CodeGenerator нічого не знає про Viewport і про розмір полотна —
 * це відповідальність виконавця ({@link Renderer}), а не генератора
 * команд.
 */
public class CodeGenerator {

    public List<GraphicsCommand> generate(GeometryModel model) {
        List<GraphicsCommand> commands = new ArrayList<>();

        for (GeometryModel.Triangle t : model.triangles()) {
            commands.add(new GraphicsCommand.DrawPolygon(List.of(
                    new double[]{t.a().x(), t.a().y()},
                    new double[]{t.b().x(), t.b().y()},
                    new double[]{t.c().x(), t.c().y()}
            )));
        }

        for (GeometryModel.Segment s : model.segments()) {
            commands.add(new GraphicsCommand.DrawSegment(
                    s.start().x(), s.start().y(), s.end().x(), s.end().y()));
        }

        for (GeometryModel.Line l : model.lines()) {
            commands.add(new GraphicsCommand.DrawInfiniteLine(
                    l.a().x(), l.a().y(),
                    l.b().x() - l.a().x(), l.b().y() - l.a().y()));
        }

        for (GeometryModel.Perpendicular p : model.perpendiculars()) {
            double dx = p.baseB().x() - p.baseA().x();
            double dy = p.baseB().y() - p.baseA().y();
            // напрямок перпендикуляра — поворот базового вектора на 90°,
            // пряма проходить через цільову точку
            commands.add(new GraphicsCommand.DrawInfiniteLine(
                    p.target().x(), p.target().y(), -dy, dx));
        }

        for (GeometryModel.Point p : model.points().values()) {
            commands.add(new GraphicsCommand.DrawPoint(p.x(), p.y(), p.name()));
        }

        return commands;
    }
}
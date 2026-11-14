package sema;

import ast.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Семантичний аналізатор.
 *
 * <p>Проходить {@link Stmt.Program} і перевіряє геометричну логіку:
 * <ul>
 *   <li>точка не оголошується двічі;</li>
 *   <li>усі точки, на які посилаються команди, справді оголошені раніше;</li>
 *   <li>пряма, перпендикуляр і відрізок не будуються між точками з
 *       однаковими координатами (це дало б нульовий напрямний вектор).</li>
 * </ul>
 *
 * <p>Перевірки, специфічні для трикутника (унікальність вершин і
 * ненульова площа), винесені в {@link TriangleValidator}.
 *
 * <p>Паралельно з перевірками будує {@link GeometryModel} — розв'язану
 * модель, де замість імен уже стоять об'єкти {@link GeometryModel.Point}.
 * Модель і є виходом цього етапу.
 */
public class SemanticAnalyzer {
    private final Map<String, GeometryModel.Point> points = new HashMap<>();
    private final List<GeometryModel.Line> lines = new ArrayList<>();
    private final List<GeometryModel.Perpendicular> perpendiculars = new ArrayList<>();
    private final List<GeometryModel.Triangle> triangles = new ArrayList<>();
    private final List<GeometryModel.Segment> segments = new ArrayList<>();

    /** Головний метод: обходить усі речення і повертає валідовану модель. */
    public GeometryModel analyze(Stmt.Program program) {
        for (Stmt stmt : program.statements()) {
            if (stmt instanceof Stmt.PointDecl p) visitPoint(p);
            else if (stmt instanceof Stmt.LineThrough l) visitLine(l);
            else if (stmt instanceof Stmt.Perpendicular p) visitPerpendicular(p);
            else if (stmt instanceof Stmt.Triangle t) visitTriangle(t);
            else if (stmt instanceof Stmt.Segment s) visitSegment(s);
        }
        return new GeometryModel(points, lines, perpendiculars, triangles, segments);
    }

    private void visitPoint(Stmt.PointDecl p) {
        if (points.containsKey(p.name())) throw new SemanticException("Точка '" + p.name() + "' вже оголошена");
        points.put(p.name(), new GeometryModel.Point(p.name(), p.x(), p.y()));
    }

    private void visitLine(Stmt.LineThrough l) {
        GeometryModel.Point a = getPoint(l.a());
        GeometryModel.Point b = getPoint(l.b());
        checkDistinct(a, b);
        lines.add(new GeometryModel.Line(a, b));
    }

    private void visitPerpendicular(Stmt.Perpendicular p) {
        GeometryModel.Point baseA = getPoint(p.baseA());
        GeometryModel.Point baseB = getPoint(p.baseB());
        GeometryModel.Point target = getPoint(p.targetPoint());
        checkDistinct(baseA, baseB);
        perpendiculars.add(new GeometryModel.Perpendicular(baseA, baseB, target));
    }

    private void visitTriangle(Stmt.Triangle t) {
        GeometryModel.Point a = getPoint(t.a());
        GeometryModel.Point b = getPoint(t.b());
        GeometryModel.Point c = getPoint(t.c());
        TriangleValidator.validate(a, b, c);
        triangles.add(new GeometryModel.Triangle(a, b, c));
    }

    private void visitSegment(Stmt.Segment s) {
        GeometryModel.Point a = getPoint(s.a());
        GeometryModel.Point b = getPoint(s.b());
        checkDistinct(a, b);
        segments.add(new GeometryModel.Segment(a, b));
    }

    private void checkDistinct(GeometryModel.Point a, GeometryModel.Point b) {
        if (a.x() == b.x() && a.y() == b.y()) {
            throw new SemanticException("Точки '" + a.name() + "' та '" + b.name() + "' мають однакові координати");
        }
    }

    private GeometryModel.Point getPoint(String name) {
        GeometryModel.Point p = points.get(name);
        if (p == null) throw new SemanticException("Точка '" + name + "' не оголошена");
        return p;
    }
}

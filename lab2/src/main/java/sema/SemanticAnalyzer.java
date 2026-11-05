package sema;

import ast.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticAnalyzer {
    private final Map<String, GeometryModel.Point> points = new HashMap<>();
    private final List<GeometryModel.Segment> lines = new ArrayList<>();
    private final List<GeometryModel.Segment> perpendiculars = new ArrayList<>();
    private final List<GeometryModel.Triangle> triangles = new ArrayList<>();

    public GeometryModel analyze(Stmt.Program program) {
        for (Stmt stmt : program.statements()) {
            if (stmt instanceof Stmt.PointDecl p) {
                visitPoint(p);
            } else if (stmt instanceof Stmt.LineThrough l) {
                visitLine(l);
            } else if (stmt instanceof Stmt.Perpendicular p) {
                visitPerpendicular(p);
            } else if (stmt instanceof Stmt.Triangle t) {
                visitTriangle(t);
            } else {
                throw new SemanticException("Невідомий тип команди: " + stmt.getClass().getSimpleName());
            }
        }
        return new GeometryModel(points, lines, perpendiculars, triangles);
    }

    private void visitPoint(Stmt.PointDecl p) {
        if (points.containsKey(p.name())) {
            throw new SemanticException("Точка '" + p.name() + "' вже була оголошена раніше");
        }
        points.put(p.name(), new GeometryModel.Point(p.name(), p.x(), p.y()));
    }

    private void visitLine(Stmt.LineThrough l) {
        GeometryModel.Point a = getPoint(l.a());
        GeometryModel.Point b = getPoint(l.b());
        if (l.a().equals(l.b())) {
            throw new SemanticException("Неможливо провести пряму через одну й ту саму точку '" + l.a() + "'");
        }
        lines.add(new GeometryModel.Segment(a, b));
    }

    private void visitPerpendicular(Stmt.Perpendicular p) {
        GeometryModel.Point baseA = getPoint(p.baseA());
        GeometryModel.Point baseB = getPoint(p.baseB());
        GeometryModel.Point target = getPoint(p.targetPoint());

        if (p.baseA().equals(p.baseB())) {
            throw new SemanticException("Базова пряма для перпендикуляра не може бути задана однією точкою");
        }
        perpendiculars.add(new GeometryModel.Segment(target, baseA));
    }

    private void visitTriangle(Stmt.Triangle t) {
        GeometryModel.Point a = getPoint(t.a());
        GeometryModel.Point b = getPoint(t.b());
        GeometryModel.Point c = getPoint(t.c());

        Set<String> uniqueNames = new HashSet<>();
        uniqueNames.add(t.a());
        uniqueNames.add(t.b());
        uniqueNames.add(t.c());
        if (uniqueNames.size() < 3) {
            throw new SemanticException("Трикутник не може мати повторювані вершини");
        }

        // Формула площі трикутника через координати (метод Гауса/детермінанта)
        double area = Math.abs((b.x() - a.x()) * (c.y() - a.y()) - (c.x() - a.x()) * (b.y() - a.y())) / 2.0;
        if (area < 1e-9) {
            throw new SemanticException("Точки " + t.a() + ", " + t.b() + ", " + t.c() + " лежать на одній прямій, трикутник вироджений");
        }

        triangles.add(new GeometryModel.Triangle(a, b, c));
    }

    private GeometryModel.Point getPoint(String name) {
        GeometryModel.Point p = points.get(name);
        if (p == null) {
            throw new SemanticException("Точка '" + name + "' не була оголошена");
        }
        return p;
    }
}
package sema;

import java.util.List;
import java.util.Map;

public record GeometryModel(
        Map<String, Point> points,
        List<Segment> lines,
        List<Segment> perpendiculars,
        List<Triangle> triangles
) {
    // Компактний конструктор для створення незмінних (immutable) копій
    public GeometryModel {
        points = Map.copyOf(points);
        lines = List.copyOf(lines);
        perpendiculars = List.copyOf(perpendiculars);
        triangles = List.copyOf(triangles);
    }

    public record Point(String name, double x, double y) {}
    public record Segment(Point start, Point end) {}
    public record Triangle(Point a, Point b, Point c) {}
}
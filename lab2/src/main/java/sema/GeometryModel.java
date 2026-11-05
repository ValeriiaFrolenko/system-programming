package sema;

import java.util.List;
import java.util.Map;

public record GeometryModel(
        Map<String, Point> points,
        List<Line> lines,
        List<Perpendicular> perpendiculars,
        List<Triangle> triangles,
        List<Segment> segments
) {
    public GeometryModel {
        points = Map.copyOf(points);
        lines = List.copyOf(lines);
        perpendiculars = List.copyOf(perpendiculars);
        triangles = List.copyOf(triangles);
        segments = List.copyOf(segments);
    }

    public record Point(String name, double x, double y) {}
    public record Line(Point a, Point b) {}
    public record Perpendicular(Point baseA, Point baseB, Point target) {}
    public record Triangle(Point a, Point b, Point c) {}
    public record Segment(Point start, Point end) {}
}

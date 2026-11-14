package ast;

import java.util.List;

public sealed interface Stmt {
    record Program(List<Stmt> statements) implements Stmt {}
    record PointDecl(String name, double x, double y) implements Stmt {}
    record LineThrough(String a, String b) implements Stmt {}
    record Perpendicular(String baseA, String baseB, String targetPoint) implements Stmt {}
    record Triangle(String a, String b, String c) implements Stmt {}
    record Segment(String a, String b) implements Stmt {}
}
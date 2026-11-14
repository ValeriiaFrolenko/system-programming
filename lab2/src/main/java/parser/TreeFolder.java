package parser;

import ast.ParseNode;
import ast.Stmt;
import lexer.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Згортає дерево синтаксичного розбору {@link ParseNode} у абстрактне
 * синтаксичне дерево {@link Stmt}. Це окремий прохід після побудови
 * дерева розбору: він не аналізує граматику, а лише перекладає вже
 * розібрану структуру речень у зручне для семантичного аналізу
 * представлення.
 */
public class TreeFolder {

    public Stmt.Program fold(ParseNode.Program program) {
        List<Stmt> statements = new ArrayList<>();
        for (ParseNode.Sentence sentence : program.sentences()) {
            statements.add(foldSentence(sentence));
        }
        return new Stmt.Program(statements);
    }

    private Stmt foldSentence(ParseNode.Sentence sentence) {
        ParseNode body = sentence.body();
        return switch (body) {
            case ParseNode.PointDecl p -> foldPointDecl(p);
            case ParseNode.LineThrough l -> foldLineThrough(l);
            case ParseNode.Perpendicular p -> foldPerpendicular(p);
            case ParseNode.Triangle t -> foldTriangle(t);
            case ParseNode.Segment s -> foldSegment(s);
            default -> throw new ParseException(
                    extractAnyToken(body),
                    "Непідтримуваний вид речення: код нетермінала " + body.code());
        };
    }

    private Stmt.PointDecl foldPointDecl(ParseNode.PointDecl p) {
        String name = nameOf(p.name());
        double x = numberOf(p.x());
        double y = numberOf(p.y());
        return new Stmt.PointDecl(name, x, y);
    }

    private Stmt.LineThrough foldLineThrough(ParseNode.LineThrough l) {
        return new Stmt.LineThrough(nameOf(l.points().a()), nameOf(l.points().b()));
    }

    private Stmt.Perpendicular foldPerpendicular(ParseNode.Perpendicular p) {
        return new Stmt.Perpendicular(
                nameOf(p.base().a()),
                nameOf(p.base().b()),
                nameOf(p.target()));
    }

    private Stmt.Triangle foldTriangle(ParseNode.Triangle t) {
        List<ParseNode.Name> vertices = t.vertices().names();
        if (vertices.size() != 3) {
            throw new ParseException(
                    vertices.getFirst().token(),
                    "Трикутник повинен мати рівно три вершини, отримано: " + vertices.size());
        }
        return new Stmt.Triangle(
                nameOf(vertices.get(0)),
                nameOf(vertices.get(1)),
                nameOf(vertices.get(2)));
    }

    private Stmt.Segment foldSegment(ParseNode.Segment s) {
        return new Stmt.Segment(nameOf(s.from()), nameOf(s.to()));
    }

    private String nameOf(ParseNode.Name name) {
        return name.token().text();
    }

    private double numberOf(ParseNode.NumberLit number) {
        return Double.parseDouble(number.token().text());
    }

    /** Допоміжний метод для повідомлень про помилки, коли вузол має невідомий тип. */
    private Token extractAnyToken(ParseNode node) {
        return switch (node) {
            case ParseNode.Name n -> n.token();
            case ParseNode.NumberLit n -> n.token();
            default -> new Token(lexer.TokenType.EOF, "", 0, 0);
        };
    }
}
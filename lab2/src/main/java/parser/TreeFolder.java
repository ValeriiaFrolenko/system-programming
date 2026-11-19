package parser;

import ast.ParseNode;
import ast.Stmt;
import lexer.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Згортає дерево синтаксичного розбору {@link ParseNode} у спрощене AST
 * {@link Stmt}.
 *
 * <p>Окремий прохід після {@link Parser}: не аналізує граматику, а лише
 * «сплющує» вже розібрану структуру, прибираючи службові вузли
 * ({@code Sentence}, {@code SentenceList}, {@code Build}, {@code NamePair},
 * {@code NameList}, {@code Name}, {@code NumberLit}) і перетворюючи
 * текстові токени на зручні типи ({@code String} для імен, {@code double}
 * для координат).
 *
 * <p>Тут же — єдина перевірка, яку зручніше зробити на цьому рівні:
 * кількість вершин трикутника мусить бути рівно 3. Усе інше (чи існують
 * точки, чи не збігаються координати) перевіряє вже
 * {@link sema.SemanticAnalyzer}.
 */
public class TreeFolder {

    /** Згортає все дерево в {@link Stmt.Program}. */
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
            case ParseNode.Build b -> foldBuild(b);
            default -> throw new ParseException(
                    extractAnyToken(body),
                    "Непідтримуваний вид речення: код нетермінала " + body.code());
        };
    }

    private Stmt foldBuild(ParseNode.Build build) {
        return switch (build.kind()) {
            case ParseNode.Perpendicular p -> foldPerpendicular(p);
            case ParseNode.Triangle t -> foldTriangle(t);
            case ParseNode.Segment s -> foldSegment(s);
        };
    }

    private Stmt.PointDecl foldPointDecl(ParseNode.PointDecl p) {
        return new Stmt.PointDecl(nameOf(p.name()), numberOf(p.x()), numberOf(p.y()));
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

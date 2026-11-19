package parser;

import ast.ParseNode;
import ast.Stmt;
import grammar.NonTerminal;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static grammar.GeometryGrammar.BUILD;
import static grammar.GeometryGrammar.BUILD_KIND;
import static grammar.GeometryGrammar.LINE_THROUGH;
import static grammar.GeometryGrammar.NAME;
import static grammar.GeometryGrammar.NAME_LIST;
import static grammar.GeometryGrammar.NAME_PAIR;
import static grammar.GeometryGrammar.PERPENDICULAR;
import static grammar.GeometryGrammar.POINT_DECL;
import static grammar.GeometryGrammar.PROGRAM;
import static grammar.GeometryGrammar.SEGMENT;
import static grammar.GeometryGrammar.SENTENCE;
import static grammar.GeometryGrammar.SENTENCE_LIST;
import static grammar.GeometryGrammar.SENTENCE_LIST_TAIL;
import static grammar.GeometryGrammar.TRIANGLE;

/**
 * Згортає дерево розбору в спрощене AST {@link Stmt}.
 *
 * <p>Це семантичний етап після {@link Parser}. Це єдине місце, що
 * знає, що означають нетермінали {@link grammar.GeometryGrammar}:
 * список речень розгортається, і кожен нетермінал-речення відображається
 * у {@link Stmt} власним методом згортання. Щоб додати нове речення,
 * додайте його метод і один запис до мапи в конструкторі.
 *
 * <p>Єдина перевірка тут - що трикутник має рівно три вершини.
 * Усе інше (чи існують точки, чи різні координати) перевіряється пізніше
 * у {@link sema.SemanticAnalyzer}.
 */
public class TreeFolder {

    private final Map<NonTerminal, Function<ParseNode.Branch, Stmt>> statementFolders;

    public TreeFolder() {
        this.statementFolders = Map.of(
                POINT_DECL, this::foldPointDecl,
                LINE_THROUGH, this::foldLineThrough,
                PERPENDICULAR, this::foldPerpendicular,
                TRIANGLE, this::foldTriangle,
                SEGMENT, this::foldSegment);
    }

    /**
     * Згортає все дерево.
     *
     * @param root корінь, повернутий {@link Parser#parse(List)}
     * @throws ParseException якщо трикутник не має трьох вершин
     */
    public Stmt.Program fold(ParseNode root) {
        if (!(root instanceof ParseNode.Branch program) || !program.is(PROGRAM)) {
            throw new IllegalArgumentException("Parse tree must be rooted at " + PROGRAM);
        }

        List<Stmt> statements = new ArrayList<>();
        ParseNode.Branch list = program.child(SENTENCE_LIST);
        while (true) {
            statements.add(foldSentence(list.child(SENTENCE)));
            Optional<ParseNode.Branch> next = list.child(SENTENCE_LIST_TAIL).findChild(SENTENCE_LIST);
            if (next.isEmpty()) {
                break;
            }
            list = next.get();
        }
        return new Stmt.Program(statements);
    }

    private Stmt foldSentence(ParseNode.Branch sentence) {
        ParseNode.Branch body = sentence.onlyChildBranch();
        if (body.is(BUILD)) {
            body = body.child(BUILD_KIND).onlyChildBranch();
        }

        Function<ParseNode.Branch, Stmt> folder = statementFolders.get(body.symbol());
        if (folder == null) {
            throw new ParseException(firstToken(body), "No folding rule for " + body.symbol());
        }
        return folder.apply(body);
    }

    private Stmt foldPointDecl(ParseNode.Branch node) {
        List<Token> numbers = tokensOf(node, TokenType.NUMBER);
        return new Stmt.PointDecl(
                tokensOf(node, TokenType.IDENTIFIER).getFirst().text(),
                Double.parseDouble(numbers.get(0).text()),
                Double.parseDouble(numbers.get(1).text()));
    }

    private Stmt foldLineThrough(ParseNode.Branch node) {
        List<Token> names = tokensOf(node, TokenType.IDENTIFIER);
        return new Stmt.LineThrough(names.get(0).text(), names.get(1).text());
    }

    private Stmt foldPerpendicular(ParseNode.Branch node) {
        List<Token> base = tokensOf(node.child(NAME_PAIR), TokenType.IDENTIFIER);
        Token target = tokensOf(node.child(NAME), TokenType.IDENTIFIER).getFirst();
        return new Stmt.Perpendicular(base.get(0).text(), base.get(1).text(), target.text());
    }

    private Stmt foldTriangle(ParseNode.Branch node) {
        List<Token> vertices = tokensOf(node.child(NAME_LIST), TokenType.IDENTIFIER);
        if (vertices.size() != 3) {
            throw new ParseException(vertices.getFirst(),
                    "A triangle must have exactly three vertices, got: " + vertices.size());
        }
        return new Stmt.Triangle(vertices.get(0).text(), vertices.get(1).text(), vertices.get(2).text());
    }

    private Stmt foldSegment(ParseNode.Branch node) {
        List<Token> names = tokensOf(node, TokenType.IDENTIFIER);
        return new Stmt.Segment(names.get(0).text(), names.get(1).text());
    }

    private static List<Token> tokensOf(ParseNode.Branch node, TokenType type) {
        return node.tokens().stream().filter(token -> token.type() == type).toList();
    }

    private static Token firstToken(ParseNode.Branch node) {
        List<Token> tokens = node.tokens();
        return tokens.isEmpty() ? new Token(TokenType.EOF, "", 0, 0) : tokens.getFirst();
    }
}

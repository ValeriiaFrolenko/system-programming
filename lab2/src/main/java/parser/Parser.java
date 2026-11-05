package parser;

import ast.Stmt;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Stmt.Program parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new Stmt.Program(statements);
    }

    private Stmt parseStatement() {
        Token token = peek();
        return switch (token.type()) {
            case POINT -> parsePoint();
            case DRAW -> parseLineThrough();
            case BUILD -> parseBuild();
            default -> throw new ParseException(token, "Очікувався початок команди");
        };
    }

    private Stmt.PointDecl parsePoint() {
        expect(TokenType.POINT);
        String name = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.LPAREN);
        double x = parseNumber();
        expect(TokenType.COMMA);
        double y = parseNumber();
        expect(TokenType.RPAREN);
        return new Stmt.PointDecl(name, x, y);
    }

    private Stmt.LineThrough parseLineThrough() {
        expect(TokenType.DRAW);
        expect(TokenType.LINE);
        expect(TokenType.THROUGH);
        String a = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.COMMA);
        String b = expect(TokenType.IDENTIFIER).text();
        return new Stmt.LineThrough(a, b);
    }

    private Stmt parseBuild() {
        expect(TokenType.BUILD);
        Token next = peek();

        if (next.type() == TokenType.PERPENDICULAR) return parsePerpendicular();
        if (next.type() == TokenType.TRIANGLE) return parseTriangle();
        if (next.type() == TokenType.SEGMENT) return parseSegment();
        
        throw new ParseException(next, "Очікувався ПЕРПЕНДИКУЛЯР, ТРИКУТНИК або ВІДРІЗОК");
    }

    private Stmt.Perpendicular parsePerpendicular() {
        expect(TokenType.PERPENDICULAR);
        expect(TokenType.TO);
        expect(TokenType.LINE);
        expect(TokenType.LPAREN);
        String baseA = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.COMMA);
        String baseB = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.RPAREN);
        expect(TokenType.AT);
        expect(TokenType.POINT_LOC);
        String target = expect(TokenType.IDENTIFIER).text();
        return new Stmt.Perpendicular(baseA, baseB, target);
    }

    private Stmt.Triangle parseTriangle() {
        expect(TokenType.TRIANGLE);
        expect(TokenType.BY);
        expect(TokenType.THREE);
        expect(TokenType.VERTEX);
        String a = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.COMMA);
        String b = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.COMMA);
        String c = expect(TokenType.IDENTIFIER).text();
        return new Stmt.Triangle(a, b, c);
    }

    private Stmt.Segment parseSegment() {
        expect(TokenType.SEGMENT);
        expect(TokenType.FROM);
        String a = expect(TokenType.IDENTIFIER).text();
        expect(TokenType.TO);
        String b = expect(TokenType.IDENTIFIER).text();
        return new Stmt.Segment(a, b);
    }

    private double parseNumber() {
        return Double.parseDouble(expect(TokenType.NUMBER).text());
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token expect(TokenType type) {
        Token token = peek();
        if (token.type() != type) {
            throw new ParseException(token, "Очікувалося " + type);
        }
        pos++;
        return token;
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }
}

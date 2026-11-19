package parser;

import ast.ParseNode;
import ast.Stmt;
import grammar.GeometryGrammar;
import grammar.Grammar;
import grammar.GrammarException;
import grammar.NonTerminal;
import grammar.Terminal;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static grammar.GeometryGrammar.BUILD;
import static grammar.GeometryGrammar.BUILD_KIND;
import static grammar.GeometryGrammar.NAME;
import static grammar.GeometryGrammar.NAME_LIST;
import static grammar.GeometryGrammar.NAME_LIST_TAIL;
import static grammar.GeometryGrammar.POINT_DECL;
import static grammar.GeometryGrammar.PROGRAM;
import static grammar.GeometryGrammar.SENTENCE;
import static grammar.GeometryGrammar.SENTENCE_LIST;
import static grammar.GeometryGrammar.SENTENCE_LIST_TAIL;
import static grammar.GeometryGrammar.TRIANGLE;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private static final Parser PARSER = new Parser(GeometryGrammar.grammar());

    private static ParseNode.Branch parseTree(String sourceWithoutEnd) {
        List<Token> tokens = new Lexer(sourceWithoutEnd + " #").tokenize();
        return assertInstanceOf(ParseNode.Branch.class, PARSER.parse(tokens));
    }

    private static Stmt.Program parseStmt(String sourceWithoutEnd) {
        return new TreeFolder().fold(parseTree(sourceWithoutEnd));
    }

    private static List<TokenType> typesOf(ParseNode node) {
        return node.tokens().stream().map(Token::type).toList();
    }

    @Test
    void rootIsTheProgramNonTerminal() {
        ParseNode.Branch root = parseTree("ТОЧКА A (1, 2)");
        assertTrue(root.is(PROGRAM));
        assertEquals(PROGRAM.code(), root.code());
    }

    @Test
    void pointDeclTreeKeepsAllTokensInSourceOrder() {
        ParseNode.Branch point = parseTree("ТОЧКА A (1.5, -2)")
                .child(SENTENCE_LIST).child(SENTENCE).child(POINT_DECL);

        assertEquals(List.of(TokenType.POINT, TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.NUMBER,
                TokenType.COMMA, TokenType.NUMBER, TokenType.RPAREN), typesOf(point));
        assertEquals("A", point.tokens().get(1).text());
        assertEquals("1.5", point.tokens().get(3).text());
        assertEquals("-2", point.tokens().get(5).text());
    }

    @Test
    void buildNestsItsKindUnderBuildKind() {
        ParseNode.Branch triangle = parseTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C")
                .child(SENTENCE_LIST).child(SENTENCE).child(BUILD).child(BUILD_KIND).child(TRIANGLE);

        assertTrue(triangle.findChild(NAME_LIST).isPresent());
    }

    @Test
    void nameListIsRecursiveThroughItsTail() {
        ParseNode.Branch nameList = parseTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C")
                .child(SENTENCE_LIST).child(SENTENCE).child(BUILD).child(BUILD_KIND)
                .child(TRIANGLE).child(NAME_LIST);

        assertTrue(nameList.findChild(NAME).isPresent());
        assertTrue(nameList.child(NAME_LIST_TAIL).findChild(NAME_LIST).isPresent());
    }

    @Test
    void lastTailIsAnEmptyBranch() {
        ParseNode.Branch list = parseTree("ТОЧКА A (1, 2)").child(SENTENCE_LIST);
        assertTrue(list.child(SENTENCE_LIST_TAIL).children().isEmpty());
    }

    @Test
    void nonTerminalsCarryTheirCodes() {
        ParseNode.Branch root = parseTree("ТОЧКА A (1, 2)");
        ParseNode.Branch list = root.child(SENTENCE_LIST);
        ParseNode.Branch sentence = list.child(SENTENCE);

        assertEquals(0, root.code());
        assertEquals(1, list.code());
        assertEquals(3, sentence.code());
        assertEquals(6, sentence.child(POINT_DECL).code());
    }

    @Test
    void parsesPointDeclStmt() {
        Stmt.Program program = parseStmt("ТОЧКА A (1.5, -2)");
        Stmt.PointDecl point = (Stmt.PointDecl) program.statements().getFirst();
        assertEquals("A", point.name());
        assertEquals(1.5, point.x());
        assertEquals(-2.0, point.y());
    }

    @Test
    void parsesLineThroughStmt() {
        Stmt.Program program = parseStmt("ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B");
        Stmt.LineThrough line = (Stmt.LineThrough) program.statements().getFirst();
        assertEquals("A", line.a());
        assertEquals("B", line.b());
    }

    @Test
    void parsesPerpendicularStmt() {
        Stmt.Program program = parseStmt("ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C");
        Stmt.Perpendicular perp = (Stmt.Perpendicular) program.statements().getFirst();
        assertEquals("A", perp.baseA());
        assertEquals("B", perp.baseB());
        assertEquals("C", perp.targetPoint());
    }

    @Test
    void parsesTriangleStmt() {
        Stmt.Program program = parseStmt("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");
        Stmt.Triangle triangle = (Stmt.Triangle) program.statements().getFirst();
        assertEquals("A", triangle.a());
        assertEquals("B", triangle.b());
        assertEquals("C", triangle.c());
    }

    @Test
    void parsesSegmentStmt() {
        Stmt.Program program = parseStmt("ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B");
        Stmt.Segment seg = (Stmt.Segment) program.statements().getFirst();
        assertEquals("A", seg.a());
        assertEquals("B", seg.b());
    }

    @Test
    void parsesFullProgramWithSemicolons() {
        String source = """
                ТОЧКА A (1, 2);
                ТОЧКА B (5, 2);
                ТОЧКА C (3, 5);
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B;
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C;
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C;
                ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B
                """;
        Stmt.Program program = parseStmt(source);
        assertEquals(7, program.statements().size());
        assertInstanceOf(Stmt.PointDecl.class, program.statements().get(0));
        assertInstanceOf(Stmt.LineThrough.class, program.statements().get(3));
        assertInstanceOf(Stmt.Perpendicular.class, program.statements().get(4));
        assertInstanceOf(Stmt.Triangle.class, program.statements().get(5));
        assertInstanceOf(Stmt.Segment.class, program.statements().get(6));
    }

    @Test
    void throwsOnMissingComma() {
        assertThrows(ParseException.class, () -> parseTree("ТОЧКА A (1 2)"));
    }

    @Test
    void throwsOnInvalidSentenceStart() {
        ParseException error = assertThrows(ParseException.class, () -> parseTree("ПРЯМУ ЧЕРЕЗ A, B"));
        assertTrue(error.getMessage().contains("Expected one of"));
    }

    @Test
    void throwsOnIncompleteBuild() {
        assertThrows(ParseException.class, () -> parseTree("ПОБУДУВАТИ ТРИКУТНИК"));
    }

    @Test
    void throwsOnEmptyProgram() {
        assertThrows(ParseException.class, () -> PARSER.parse(new Lexer("#").tokenize()));
    }

    @Test
    void throwsWhenMissingEndMarker() {
        List<Token> tokens = new Lexer("ТОЧКА A (1, 2)").tokenize();
        assertThrows(ParseException.class, () -> PARSER.parse(tokens));
    }

    @Test
    void throwsWhenMissingSemicolonBetweenSentences() {
        List<Token> tokens = new Lexer("ТОЧКА A (1, 2) ТОЧКА B (3, 4) #").tokenize();
        assertThrows(ParseException.class, () -> PARSER.parse(tokens));
    }

    @Test
    void throwsOnTriangleWithWrongVertexCount() {
        assertThrows(ParseException.class,
                () -> parseStmt("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B"));
    }

    @Test
    void rejectsTokenListWithoutEof() {
        List<Token> withoutEof = new Lexer("ТОЧКА A (1, 2) #").tokenize().stream()
                .filter(token -> token.type() != TokenType.EOF)
                .toList();
        assertThrows(IllegalArgumentException.class, () -> PARSER.parse(withoutEof));
    }

    @Test
    void parsesAnotherLanguageWithoutAnyChangeToTheParser() {
        Grammar names = commaSeparatedNames();
        ParseNode root = new Parser(names).parse(new Lexer("A, B, C #").tokenize());

        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.COMMA, TokenType.IDENTIFIER,
                        TokenType.COMMA, TokenType.IDENTIFIER, TokenType.END),
                typesOf(root));
    }

    @Test
    void reportsSyntaxErrorsOfAnotherLanguage() {
        Parser parser = new Parser(commaSeparatedNames());
        List<Token> tokens = new Lexer("A, #").tokenize();
        assertThrows(ParseException.class, () -> parser.parse(tokens));
    }

    @Test
    void refusesToCreateParserForNonLl1Grammar() {
        NonTerminal start = new NonTerminal(0, "Start");
        Grammar ambiguous = Grammar.builder(start)
                .rule(start, new Terminal(TokenType.IDENTIFIER), new Terminal(TokenType.END))
                .rule(start, new Terminal(TokenType.IDENTIFIER), new Terminal(TokenType.COMMA))
                .build();
        assertThrows(GrammarException.class, () -> new Parser(ambiguous));
    }

    private static Grammar commaSeparatedNames() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal list = new NonTerminal(1, "List");
        NonTerminal tail = new NonTerminal(2, "Tail");
        return Grammar.builder(start)
                .rule(start, list, new Terminal(TokenType.END))
                .rule(list, new Terminal(TokenType.IDENTIFIER), tail)
                .rule(tail, new Terminal(TokenType.COMMA), list)
                .rule(tail)
                .build();
    }
}

package parser;

import ast.ParseNode;
import ast.Stmt;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private static ParseNode.Program parseTree(String sourceWithoutEnd) {
        String source = sourceWithoutEnd + " #";
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    private static Stmt.Program parseStmt(String sourceWithoutEnd) {
        return new TreeFolder().fold(parseTree(sourceWithoutEnd));
    }

    @Test
    void parsesPointDeclTree() {
        ParseNode.Program program = parseTree("ТОЧКА A (1.5, -2)");
        assertEquals(1, program.sentences().size());
        ParseNode.Sentence sentence = program.sentences().getFirst();
        assertInstanceOf(ParseNode.PointDecl.class, sentence.body());
        ParseNode.PointDecl point = (ParseNode.PointDecl) sentence.body();
        assertEquals("A", point.name().token().text());
        assertEquals("1.5", point.x().token().text());
        assertEquals("-2", point.y().token().text());
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
    void nameListCarriesAllNamesInParseTree() {
        ParseNode.Program program = parseTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");
        ParseNode.Build build = (ParseNode.Build) program.sentences().getFirst().body();
        ParseNode.Triangle triangle = (ParseNode.Triangle) build.kind();
        assertEquals(3, triangle.vertices().names().size());
    }

    @Test
    void nonTerminalsCarryExpectedCodes() {
        ParseNode.Program program = parseTree("ТОЧКА A (1, 2)");
        assertEquals(ParseNode.Program.CODE, program.code());
        assertEquals(ParseNode.SentenceList.CODE, program.sentenceList().code());
        ParseNode.Sentence sentence = program.sentences().getFirst();
        assertEquals(ParseNode.Sentence.CODE, sentence.code());
        assertEquals(ParseNode.PointDecl.CODE, sentence.body().code());
    }

    @Test
    void buildNodeCarriesExpectedCode() {
        ParseNode.Program program = parseTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");
        ParseNode.Build build = (ParseNode.Build) program.sentences().getFirst().body();
        assertEquals(ParseNode.Build.CODE, build.code());
        assertEquals(ParseNode.Triangle.CODE, build.kind().code());
    }

    @Test
    void throwsOnMissingComma() {
        assertThrows(ParseException.class, () -> parseTree("ТОЧКА A (1 2)"));
    }

    @Test
    void throwsOnInvalidSentenceStart() {
        assertThrows(ParseException.class, () -> parseTree("ПРЯМУ ЧЕРЕЗ A, B"));
    }

    @Test
    void throwsOnIncompleteBuild() {
        assertThrows(ParseException.class, () -> parseTree("ПОБУДУВАТИ ТРИКУТНИК"));
    }

    @Test
    void throwsWhenMissingEndMarker() {
        String source = "ТОЧКА A (1, 2)"; // без #
        assertThrows(ParseException.class,
                () -> new Parser(new Lexer(source).tokenize()).parse());
    }

    @Test
    void throwsWhenMissingSemicolonBetweenSentences() {
        String source = "ТОЧКА A (1, 2) ТОЧКА B (3, 4) #";
        assertThrows(ParseException.class,
                () -> new Parser(new Lexer(source).tokenize()).parse());
    }

    @Test
    void throwsOnTriangleWithWrongVertexCount() {
        assertThrows(ParseException.class,
                () -> parseStmt("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B"));
    }
}

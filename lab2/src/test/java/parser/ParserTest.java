package parser;

import ast.Stmt;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private static Stmt.Program parse(String source) {
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    @Test
    void parsesPointDecl() {
        Stmt.Program program = parse("ТОЧКА A (1.5, -2)");
        assertEquals(1, program.statements().size());
        Stmt.PointDecl point = (Stmt.PointDecl) program.statements().getFirst();
        assertEquals("A", point.name());
        assertEquals(1.5, point.x());
        assertEquals(-2.0, point.y());
    }

    @Test
    void parsesLineThrough() {
        Stmt.Program program = parse("ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B");
        Stmt.LineThrough line = (Stmt.LineThrough) program.statements().getFirst();
        assertEquals("A", line.a());
        assertEquals("B", line.b());
    }

    @Test
    void parsesPerpendicular() {
        Stmt.Program program = parse("ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C");
        Stmt.Perpendicular perp = (Stmt.Perpendicular) program.statements().getFirst();
        assertEquals("A", perp.baseA());
        assertEquals("B", perp.baseB());
        assertEquals("C", perp.targetPoint());
    }

    @Test
    void parsesTriangle() {
        Stmt.Program program = parse("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");
        Stmt.Triangle triangle = (Stmt.Triangle) program.statements().getFirst();
        assertEquals("A", triangle.a());
        assertEquals("B", triangle.b());
        assertEquals("C", triangle.c());
    }

    @Test
    void parsesSegment() {
        Stmt.Program program = parse("ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B");
        Stmt.Segment seg = (Stmt.Segment) program.statements().getFirst();
        assertEquals("A", seg.a());
        assertEquals("B", seg.b());
    }

    @Test
    void parsesFullProgram() {
        String source = """
                ТОЧКА A (1, 2)
                ТОЧКА B (5, 2)
                ТОЧКА C (3, 5)
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C
                ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B
                """;
        Stmt.Program program = parse(source);
        assertEquals(7, program.statements().size());
        assertInstanceOf(Stmt.PointDecl.class, program.statements().get(0));
        assertInstanceOf(Stmt.LineThrough.class, program.statements().get(3));
        assertInstanceOf(Stmt.Perpendicular.class, program.statements().get(4));
        assertInstanceOf(Stmt.Triangle.class, program.statements().get(5));
        assertInstanceOf(Stmt.Segment.class, program.statements().get(6));
    }

    @Test
    void throwsOnMissingComma() {
        assertThrows(ParseException.class, () -> parse("ТОЧКА A (1 2)"));
    }

    @Test
    void throwsOnInvalidCommandStart() {
        assertThrows(ParseException.class, () -> parse("ПРЯМУ ЧЕРЕЗ A, B"));
    }

    @Test
    void throwsOnIncompleteBuild() {
        assertThrows(ParseException.class, () -> parse("ПОБУДУВАТИ ТРИКУТНИК"));
    }
}

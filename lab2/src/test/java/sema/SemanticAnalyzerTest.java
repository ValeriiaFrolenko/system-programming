package sema;

import ast.Stmt;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SemanticAnalyzerTest {

    private static GeometryModel analyze(String source) {
        Stmt.Program program = new Parser(new Lexer(source).tokenize()).parse();
        return new SemanticAnalyzer().analyze(program);
    }

    @Test
    void analyzesValidProgram() {
        String source = """
                ТОЧКА A (1, 2)
                ТОЧКА B (5, 2)
                ТОЧКА C (3, 5)
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C
                """;
        GeometryModel model = analyze(source);

        // Тепер використовуємо points() замість getPoints()
        assertEquals(3, model.points().size());
        assertEquals(1, model.lines().size());
        assertEquals(1, model.perpendiculars().size());
        assertEquals(1, model.triangles().size());
    }

    @Test
    void throwsOnDuplicatePoint() {
        String source = """
                ТОЧКА A (1, 2)
                ТОЧКА A (3, 4)
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnUndeclaredPointInLine() {
        String source = """
                ТОЧКА A (1, 2)
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnLineWithSamePoints() {
        String source = """
                ТОЧКА A (1, 2)
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, A
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnTriangleWithSamePoints() {
        String source = """
                ТОЧКА A (1, 2)
                ТОЧКА B (5, 2)
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, B
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnDegenerateTriangle() {
        String source = """
                ТОЧКА A (1, 1)
                ТОЧКА B (2, 2)
                ТОЧКА C (3, 3)
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnUndeclaredPointInPerpendicular() {
        String source = """
                ТОЧКА A (1, 2)
                ТОЧКА B (5, 2)
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C
                """;
        assertThrows(SemanticException.class, () -> analyze(source));
    }
}
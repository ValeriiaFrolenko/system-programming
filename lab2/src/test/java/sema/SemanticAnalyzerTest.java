package sema;

import ast.ParseNode;
import ast.Stmt;
import grammar.GeometryGrammar;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import parser.TreeFolder;
import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {
    private static GeometryModel analyze(String sourceWithoutEnd) {
        String source = sourceWithoutEnd.strip().endsWith("#")
                ? sourceWithoutEnd
                : sourceWithoutEnd + " #";
        ParseNode tree = new Parser(GeometryGrammar.grammar()).parse(new Lexer(source).tokenize());
        Stmt.Program program = new TreeFolder().fold(tree);
        return new SemanticAnalyzer().analyze(program);
    }

    @Test
    void analyzesValidProgram() {
        String source = """
                ТОЧКА A (1, 2);
                ТОЧКА B (5, 2);
                ТОЧКА C (3, 5);
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B;
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C;
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C;
                ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B
                """;
        GeometryModel model = analyze(source);
        assertEquals(3, model.points().size());
        assertEquals(1, model.lines().size());
        assertEquals(1, model.perpendiculars().size());
        assertEquals(1, model.triangles().size());
        assertEquals(1, model.segments().size());
    }

    @Test
    void throwsOnDuplicatePoint() {
        String source = "ТОЧКА A (1, 2);\nТОЧКА A (3, 4)";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnUndeclaredPointInLine() {
        String source = "ТОЧКА A (1, 2);\nПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnLineWithSamePoints() {
        String source = "ТОЧКА A (1, 2);\nПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, A";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnSameCoordinatesInLine() {
        String source = "ТОЧКА A (1, 1);\nТОЧКА B (1, 1);\nПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnTriangleWithSamePoints() {
        String source = "ТОЧКА A (1, 2);\nТОЧКА B (5, 2);\nПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, B";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnDegenerateTriangle() {
        String source = "ТОЧКА A (1, 1);\nТОЧКА B (2, 2);\nТОЧКА C (3, 3);\nПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C";
        assertThrows(SemanticException.class, () -> analyze(source));
    }

    @Test
    void throwsOnUndeclaredPointInPerpendicular() {
        String source = "ТОЧКА A (1, 2);\nТОЧКА B (5, 2);\nПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C";
        assertThrows(SemanticException.class, () -> analyze(source));
    }
}
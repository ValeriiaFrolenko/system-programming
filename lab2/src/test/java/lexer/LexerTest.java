package lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {

    private static List<Token> tokenize(String source) {
        return new Lexer(source).tokenize();
    }

    @Test
    void tokenizesPointDeclaration() {
        List<Token> tokens = tokenize("ТОЧКА A (1, 2)");
        assertEquals(TokenType.POINT, tokens.get(0).type());
        assertEquals("ТОЧКА", tokens.get(0).text());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals("A", tokens.get(1).text());
        assertEquals(TokenType.LPAREN, tokens.get(2).type());
        assertEquals(TokenType.NUMBER, tokens.get(3).type());
        assertEquals("1", tokens.get(3).text());
        assertEquals(TokenType.COMMA, tokens.get(4).type());
        assertEquals(TokenType.NUMBER, tokens.get(5).type());
        assertEquals("2", tokens.get(5).text());
        assertEquals(TokenType.RPAREN, tokens.get(6).type());
        assertEquals(TokenType.EOF, tokens.get(7).type());
    }

    @Test
    void tokenizesLineThrough() {
        List<Token> tokens = tokenize("ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B");
        assertEquals(TokenType.DRAW, tokens.get(0).type());
        assertEquals(TokenType.LINE, tokens.get(1).type());
        assertEquals(TokenType.THROUGH, tokens.get(2).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(3).type());
        assertEquals(TokenType.COMMA, tokens.get(4).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(5).type());
        assertEquals(TokenType.EOF, tokens.get(6).type());
    }

    @Test
    void tokenizesPerpendicular() {
        List<Token> tokens = tokenize("ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C");
        assertEquals(TokenType.BUILD, tokens.get(0).type());
        assertEquals(TokenType.PERPENDICULAR, tokens.get(1).type());
        assertEquals(TokenType.TO, tokens.get(2).type());
        assertEquals(TokenType.LINE, tokens.get(3).type());
        assertEquals(TokenType.LPAREN, tokens.get(4).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(5).type());
        assertEquals(TokenType.COMMA, tokens.get(6).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(7).type());
        assertEquals(TokenType.RPAREN, tokens.get(8).type());
        assertEquals(TokenType.AT, tokens.get(9).type());
        assertEquals(TokenType.POINT_LOC, tokens.get(10).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(11).type());
        assertEquals(TokenType.EOF, tokens.get(12).type());
    }

    @Test
    void tokenizesTriangle() {
        List<Token> tokens = tokenize("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");
        assertEquals(TokenType.BUILD, tokens.get(0).type());
        assertEquals(TokenType.TRIANGLE, tokens.get(1).type());
        assertEquals(TokenType.BY, tokens.get(2).type());
        assertEquals(TokenType.THREE, tokens.get(3).type());
        assertEquals(TokenType.VERTEX, tokens.get(4).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(5).type());
        assertEquals(TokenType.COMMA, tokens.get(6).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(7).type());
        assertEquals(TokenType.COMMA, tokens.get(8).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(9).type());
        assertEquals(TokenType.EOF, tokens.get(10).type());
    }

    @Test
    void recognizesMorphologicalVariants() {
        assertEquals(TokenType.POINT, tokenize("ТОЧКА").getFirst().type());
        assertEquals(TokenType.POINT, tokenize("ТОЧКИ").getFirst().type());
        assertEquals(TokenType.POINT_LOC, tokenize("ТОЧЦІ").getFirst().type());

        assertEquals(TokenType.LINE, tokenize("ПРЯМА").getFirst().type());
        assertEquals(TokenType.LINE, tokenize("ПРЯМУ").getFirst().type());
        assertEquals(TokenType.LINE, tokenize("ПРЯМОЇ").getFirst().type());

        assertEquals(TokenType.VERTEX, tokenize("ВЕРШИНА").getFirst().type());
        assertEquals(TokenType.VERTEX, tokenize("ВЕРШИНАМ").getFirst().type());
    }

    @Test
    void distinguishesShortStemsFromLongerOnes() {
        assertEquals(TokenType.BY, tokenize("ПО").getFirst().type());
        assertEquals(TokenType.BUILD, tokenize("ПОБУДУВАТИ").getFirst().type());

        assertEquals(TokenType.AT, tokenize("В").getFirst().type());
        assertEquals(TokenType.VERTEX, tokenize("ВЕРШИНАМ").getFirst().type());
    }

    @Test
    void handlesNegativeAndDecimalNumbers() {
        List<Token> tokens = tokenize("ТОЧКА A (-1.5, 2)");
        assertEquals(TokenType.NUMBER, tokens.get(3).type());
        assertEquals("-1.5", tokens.get(3).text());
        assertEquals(TokenType.NUMBER, tokens.get(5).type());
        assertEquals("2", tokens.get(5).text());
    }

    @Test
    void tracksLineAndColumn() {
        List<Token> tokens = tokenize("ТОЧКА\nA");
        assertEquals(1, tokens.get(0).line());
        assertEquals(1, tokens.get(0).column());
        assertEquals(2, tokens.get(1).line());
        assertEquals(1, tokens.get(1).column());
    }

    @Test
    void handlesCrLfLineEndings() {
        List<Token> tokens = tokenize("ТОЧКА\r\nA");
        assertEquals(2, tokens.get(1).line());
        assertEquals(1, tokens.get(1).column());
    }

    @Test
    void throwsOnUnknownWord() {
        assertThrows(LexException.class, () -> tokenize("АБРАКАДАБРА"));
    }

    @Test
    void throwsOnUnknownCharacter() {
        assertThrows(LexException.class, () -> tokenize("ТОЧКА @"));
    }

    @Test
    void returnsOnlyEofOnEmptyInput() {
        List<Token> tokens = tokenize("   \n\t  ");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.getFirst().type());
    }

    @Test
    void tokenizesFullProgram() {
        String program = """
                ТОЧКА A (1, 2)
                ТОЧКА B (5, 2)
                ТОЧКА C (3, 5)
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C
                """;
        List<Token> tokens = tokenize(program);
        assertNotNull(tokens);
        assertEquals(TokenType.EOF, tokens.getLast().type());
    }
}
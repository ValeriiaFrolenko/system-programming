package ast;

import grammar.GeometryGrammar;
import grammar.Grammar;
import grammar.NonTerminal;
import grammar.Terminal;
import lexer.Lexer;
import lexer.TokenType;
import org.junit.jupiter.api.Test;
import parser.Parser;

import static grammar.GeometryGrammar.BUILD;
import static grammar.GeometryGrammar.BUILD_KIND;
import static grammar.GeometryGrammar.LINE_THROUGH;
import static grammar.GeometryGrammar.NAME;
import static grammar.GeometryGrammar.NAME_LIST;
import static grammar.GeometryGrammar.NAME_PAIR;
import static grammar.GeometryGrammar.NUMBER;
import static grammar.GeometryGrammar.POINT_DECL;
import static grammar.GeometryGrammar.PROGRAM;
import static grammar.GeometryGrammar.SENTENCE;
import static grammar.GeometryGrammar.SENTENCE_LIST;
import static grammar.GeometryGrammar.TRIANGLE;
import static org.junit.jupiter.api.Assertions.*;

class ParseTreePrinterTest {

    private static String printTree(String sourceWithoutEnd) {
        ParseNode tree = new Parser(GeometryGrammar.grammar())
                .parse(new Lexer(sourceWithoutEnd + " #").tokenize());
        return new ParseTreePrinter().print(tree);
    }

    private static String label(NonTerminal nonTerminal) {
        return nonTerminal.name() + " [" + nonTerminal.code() + "]";
    }

    private static int count(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    @Test
    void printsNonTerminalsWithNamesAndCodes() {
        String output = printTree("ТОЧКА A (1, 2)");

        assertTrue(output.contains(label(PROGRAM)));
        assertTrue(output.contains(label(SENTENCE_LIST)));
        assertTrue(output.contains(label(SENTENCE)));
        assertTrue(output.contains(label(POINT_DECL)));
        assertTrue(output.contains(label(NAME)));
        assertTrue(output.contains(label(NUMBER)));
    }

    @Test
    void printsTerminalsAsTokens() {
        String output = printTree("ТОЧКА A (1, 2)");

        assertTrue(output.contains("POINT(\"ТОЧКА\")"));
        assertTrue(output.contains("IDENTIFIER(\"A\")"));
        assertTrue(output.contains("LPAREN(\"(\")"));
        assertTrue(output.contains("NUMBER(\"1\")"));
        assertTrue(output.contains("END(\"#\")"));
    }

    @Test
    void indentationGrowsWithDepth() {
        String[] lines = printTree("ТОЧКА A (1, 2)").split("\n");

        // Program -> SentenceList -> Sentence -> PointDecl -> first token of PointDecl
        int program = leadingSpaces(lines[0]);
        int sentenceList = leadingSpaces(lines[1]);
        int sentence = leadingSpaces(lines[2]);
        int pointDecl = leadingSpaces(lines[3]);
        int firstToken = leadingSpaces(lines[4]);

        assertEquals(0, program);
        assertTrue(sentenceList > program);
        assertTrue(sentence > sentenceList);
        assertTrue(pointDecl > sentence);
        assertTrue(firstToken > pointDecl);
    }

    @Test
    void printsAllThreeNamesForTriangle() {
        String output = printTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");

        assertTrue(output.contains(label(BUILD)));
        assertTrue(output.contains(label(BUILD_KIND)));
        assertTrue(output.contains(label(TRIANGLE)));
        assertTrue(output.contains(label(NAME_LIST)));
        assertEquals(3, count(output, label(NAME) + "\n"));
        assertEquals(3, count(output, "IDENTIFIER("));
    }

    @Test
    void printsNamePairForLineThrough() {
        String output = printTree("ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B");

        assertTrue(output.contains(label(LINE_THROUGH)));
        assertTrue(output.contains(label(NAME_PAIR)));
    }

    @Test
    void printsTreeOfAnyGrammar() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal item = new NonTerminal(7, "Item");
        Grammar grammar = Grammar.builder(start)
                .rule(start, item, new Terminal(TokenType.END))
                .rule(item, new Terminal(TokenType.IDENTIFIER))
                .build();

        String output = new ParseTreePrinter()
                .print(new Parser(grammar).parse(new Lexer("A #").tokenize()));

        assertTrue(output.startsWith("Start [0]\n"));
        assertTrue(output.contains("  Item [7]\n"));
        assertTrue(output.contains("    IDENTIFIER(\"A\")"));
    }

    private static int leadingSpaces(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') {
            i++;
        }
        return i;
    }
}

package ast;

import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;

import static org.junit.jupiter.api.Assertions.*;

class ParseTreePrinterTest {

    private static String printTree(String sourceWithoutEnd) {
        String source = sourceWithoutEnd + " #";
        ParseNode.Program tree = new Parser(new Lexer(source).tokenize()).parse();
        return new ParseTreePrinter().print(tree);
    }

    @Test
    void printsSimplePointDeclWithCodesAndTokenText() {
        String output = printTree("ТОЧКА A (1, 2)");

        assertTrue(output.contains("Program [" + ParseNode.Program.CODE + "]"));
        assertTrue(output.contains("Sentence [" + ParseNode.Sentence.CODE + "]"));
        assertTrue(output.contains("PointDecl [" + ParseNode.PointDecl.CODE + "]"));
        assertTrue(output.contains("Name [" + ParseNode.Name.CODE + "]"));
        assertTrue(output.contains("\"A\""));
        assertTrue(output.contains("NumberLit [" + ParseNode.NumberLit.CODE + "]"));
    }

    @Test
    void indentationGrowsWithDepth() {
        String output = printTree("ТОЧКА A (1, 2)");
        String[] lines = output.split("\n");

        // Program -> Sentence -> PointDecl -> Name: відступ зростає на
        // кожному рівні вкладеності.
        int programIndent = leadingSpaces(lines[0]);
        int sentenceIndent = leadingSpaces(lines[1]);
        int pointDeclIndent = leadingSpaces(lines[2]);
        int nameIndent = leadingSpaces(lines[3]);

        assertTrue(sentenceIndent > programIndent);
        assertTrue(pointDeclIndent > sentenceIndent);
        assertTrue(nameIndent > pointDeclIndent);
    }

    @Test
    void printsAllThreeNamesForTriangleNameList() {
        String output = printTree("ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C");

        assertTrue(output.contains("Triangle [" + ParseNode.Triangle.CODE + "]"));
        assertTrue(output.contains("NameList [" + ParseNode.NameList.CODE + "]"));
        assertEquals(3, output.split("Name \\[" + ParseNode.Name.CODE + "\\]").length - 1);
    }

    @Test
    void printsNamePairForLineThrough() {
        String output = printTree("ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B");

        assertTrue(output.contains("LineThrough [" + ParseNode.LineThrough.CODE + "]"));
        assertTrue(output.contains("NamePair [" + ParseNode.NamePair.CODE + "]"));
    }

    private static int leadingSpaces(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') i++;
        return i;
    }
}
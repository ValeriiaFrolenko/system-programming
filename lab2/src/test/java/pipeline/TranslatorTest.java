package pipeline;

import ast.ParseNode;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static grammar.GeometryGrammar.PROGRAM;
import static org.junit.jupiter.api.Assertions.*;

class TranslatorTest {

    private final Translator translator = new Translator();

    @Test
    void parseProducesParseTreeWithoutRendering() {
        ParseNode tree = translator.parse("ТОЧКА A (1, 2) #");
        ParseNode.Branch root = assertInstanceOf(ParseNode.Branch.class, tree);
        assertTrue(root.is(PROGRAM));
    }

    @Test
    void translatesSimpleProgramIntoBufferedImage() {
        String source = """
                ТОЧКА A (1, 2);
                ТОЧКА B (5, 2);
                ТОЧКА C (3, 5);
                ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B;
                ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ (A, B) В ТОЧЦІ C;
                ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ A, B, C;
                ПОБУДУВАТИ ВІДРІЗОК ВІД A ДО B
                #""";
        BufferedImage image = translator.translate(source);
        assertNotNull(image);
        assertEquals(800, image.getWidth());
        assertEquals(600, image.getHeight());
    }

    @Test
    void renderReusesParseTreeWithoutReparsing() {
        ParseNode tree = translator.parse("ТОЧКА A (0, 0) #");
        BufferedImage image = translator.render(tree);
        assertNotNull(image);
    }
}

package pipeline;

import ast.ParseNode;
import ast.Stmt;
import lexer.Lexer;
import parser.Parser;
import parser.TreeFolder;
import render.CodeGenerator;
import render.GraphicsCommand;
import render.Renderer;
import sema.GeometryModel;
import sema.SemanticAnalyzer;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Оркестратор повного конвеєра трансляції: джерело → токени → CST → AST
 * → семантична модель → команди малювання → зображення.
 *
 * <p>Розбитий на дві чисті операції — {@link #parse(String)} і
 * {@link #render(ParseNode.Program)} — щоб {@code Main} міг за бажанням
 * втрутитися між ними (наприклад, надрукувати дерево за {@code --tree}),
 * не перезапускаючи лексер і парсер.
 */
public class Translator {

    /** Джерело → CST. Кидає {@link lexer.LexException} / {@link parser.ParseException}. */
    public ParseNode.Program parse(String source) {
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    /** CST → зображення. Кидає {@link sema.SemanticException}. */
    public BufferedImage render(ParseNode.Program tree) {
        Stmt.Program ast = new TreeFolder().fold(tree);
        GeometryModel model = new SemanticAnalyzer().analyze(ast);
        List<GraphicsCommand> commands = new CodeGenerator().generate(model);
        return new Renderer().render(commands);
    }

    /** Зручність для тестів/скриптів: джерело → зображення за один виклик. */
    public BufferedImage translate(String source) {
        return render(parse(source));
    }
}

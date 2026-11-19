package pipeline;

import ast.ParseNode;
import ast.Stmt;
import grammar.GeometryGrammar;
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
 * Оркеструє весь конвеєр трансляції: вихідний текст, токени, дерево
 * розбору, AST, семантична модель, графічні команди, зображення.
 *
 * <p>Конвеєр розділено на дві чисті операції, {@link #parse(String)}
 * і {@link #render(ParseNode)}, щоб {@code Main} міг зробити щось між ними
 * (наприклад, надрукувати дерево з {@code --tree}), не запускаючи лексер
 * і парсер знову.
 *
 * <p>Парсер - універсальний {@link Parser}; цей клас дає йому
 * {@link GeometryGrammar}. Таблиця LL(1) будується один раз на транслятор.
 */
public class Translator {
    private final Parser parser = new Parser(GeometryGrammar.grammar());

    /**
     * Вихідний текст у дерево розбору.
     *
     * @throws lexer.LexException    при лексичній помилці
     * @throws parser.ParseException при синтаксичній помилці
     */
    public ParseNode parse(String source) {
        return parser.parse(new Lexer(source).tokenize());
    }

    /**
     * Дерево розбору в зображення.
     *
     * @throws sema.SemanticException при семантичній помилці
     */
    public BufferedImage render(ParseNode tree) {
        Stmt.Program ast = new TreeFolder().fold(tree);
        GeometryModel model = new SemanticAnalyzer().analyze(ast);
        List<GraphicsCommand> commands = new CodeGenerator().generate(model);
        return new Renderer().render(commands);
    }

    /**
     * Зручність для тестів і скриптів: вихідний текст у зображення одним викликом.
     */
    public BufferedImage translate(String source) {
        return render(parse(source));
    }
}

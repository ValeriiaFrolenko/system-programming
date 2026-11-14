import ast.ParseNode;
import ast.ParseTreePrinter;
import ast.Stmt;
import lexer.LexException;
import lexer.Lexer;
import parser.ParseException;
import parser.Parser;
import parser.TreeFolder;
import render.CodeGenerator;
import render.GraphicsCommand;
import render.Renderer;
import sema.GeometryModel;
import sema.SemanticAnalyzer;
import sema.SemanticException;

/**
 * Точка входу транслятора геометричної DSL.
 *
 * <p>Організовує повний конвеєр обробки вхідного файлу:
 * <ol>
 *   <li>{@link Lexer} — розбиття тексту на токени;</li>
 *   <li>{@link Parser} — побудова дерева синтаксичного розбору
 *       {@link ParseNode.Program};</li>
 *   <li>{@link TreeFolder} — згортання дерева у спрощене AST
 *       {@link Stmt.Program};</li>
 *   <li>{@link SemanticAnalyzer} — семантична перевірка і побудова
 *       {@link GeometryModel};</li>
 *   <li>{@link CodeGenerator} — генерація команд {@link GraphicsCommand};</li>
 *   <li>{@link Renderer} — малювання через Java2D і збереження PNG.</li>
 * </ol>
 *
 * <p>Використання: {@code java Main [input.geo] [output.png] [--tree]}.
 * Перші два аргументи — необов'язкові (за замовчуванням {@code input.geo}
 * і {@code output.png}). Прапорець {@code --tree} друкує дерево
 * синтаксичного розбору перед семантичним аналізом — зручно для
 * демонстрації стадії побудови дерева.
 *
 * <p>Помилки всіх етапів ловляться тут і друкуються в stderr; у разі
 * успіху в stdout виводиться шлях до збереженого PNG.
 */
void main(String[] args) {
    String inputPath = "input.geo";
    String outputPath = "output.png";
    boolean printTree = false;

    List<String> positional = new ArrayList<>();
    for (String arg : args) {
        if (arg.equals("--tree")) printTree = true;
        else positional.add(arg);
    }
    if (positional.size() >= 1) inputPath = positional.get(0);
    if (positional.size() >= 2) outputPath = positional.get(1);

    try {
        String source = Files.readString(Path.of(inputPath), StandardCharsets.UTF_8);

        var tokens = new Lexer(source).tokenize();
        ParseNode.Program tree = new Parser(tokens).parse();

        if (printTree) {
            IO.println(new ParseTreePrinter().print(tree));
        }

        Stmt.Program ast = new TreeFolder().fold(tree);
        GeometryModel model = new SemanticAnalyzer().analyze(ast);
        List<GraphicsCommand> commands = new CodeGenerator().generate(model);

        new Renderer().render(commands, outputPath);
        IO.println("Успіх! Зображення збережено у файл: " + outputPath);

    } catch (IOException e) {
        System.err.println("Помилка читання/запису файлу: " + e.getMessage());
    } catch (LexException | ParseException | SemanticException e) {
        System.err.println(e.getMessage());
    }
}

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

// List входить до неявного набору java.util.* цього режиму (unnamed class, JEP 495) —
// явний імпорт не додаю, як і в решті файлу (Files, Path, IOException теж неявні).

void main(String[] args) {
    String inputPath = "input.geo";
    String outputPath = "output.png";
    boolean printTree = false;

    // "--tree" шукаємо окремо, щоб він не зсував позиційні input/output
    // незалежно від того, де в argv він вказаний.
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
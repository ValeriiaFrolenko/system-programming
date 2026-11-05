import ast.Stmt;
import lexer.LexException;
import lexer.Lexer;
import parser.ParseException;
import parser.Parser;
import render.Renderer;
import sema.GeometryModel;
import sema.SemanticAnalyzer;
import sema.SemanticException;

void main(String[] args) {
    String inputPath = "input.geo";
    String outputPath = "output.png";

    if (args.length >= 1) inputPath = args[0];
    if (args.length >= 2) outputPath = args[1];

    try {
        String source = Files.readString(Path.of(inputPath), StandardCharsets.UTF_8);

        var tokens = new Lexer(source).tokenize();
        Stmt.Program ast = new Parser(tokens).parse();
        GeometryModel model = new SemanticAnalyzer().analyze(ast);

        new Renderer().render(model, outputPath);
        IO.println("Успіх! Зображення збережено у файл: " + outputPath);

    } catch (IOException e) {
        System.err.println("Помилка читання/запису файлу: " + e.getMessage());
    } catch (LexException | ParseException | SemanticException e) {
        System.err.println(e.getMessage());
    }
}
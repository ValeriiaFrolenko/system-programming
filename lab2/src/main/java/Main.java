import ast.ParseNode;
import ast.ParseTreePrinter;
import lexer.LexException;
import parser.ParseException;
import pipeline.Translator;
import sema.SemanticException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Точка входу транслятора геометричної DSL.
 *
 * <p>Тонкий шар «аргументи + файловий I/O + діагностика»: сам конвеєр
 * живе у {@link Translator}. {@code Main} лише читає джерело, за
 * потреби друкує CST ({@code --tree}), викликає {@link Translator#parse}
 * і {@link Translator#render}, зберігає PNG і ловить помилки всіх етапів.
 *
 * <p>Використання: {@code java Main [input.geo] [output.png] [--tree]}.
 * Перші два аргументи — необов'язкові (за замовчуванням {@code input.geo}
 * і {@code output.png}). Прапорець {@code --tree} друкує дерево
 * синтаксичного розбору перед семантичним аналізом — зручно для
 * демонстрації стадії побудови дерева.
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
        Translator translator = new Translator();

        ParseNode.Program tree = translator.parse(source);
        if (printTree) {
            IO.println(new ParseTreePrinter().print(tree));
        }

        BufferedImage image = translator.render(tree);
        ImageIO.write(image, "png", new File(outputPath));
        IO.println("Успіх! Зображення збережено у файл: " + outputPath);

    } catch (IOException e) {
        System.err.println("Помилка читання/запису файлу: " + e.getMessage());
    } catch (LexException | ParseException | SemanticException e) {
        System.err.println(e.getMessage());
    }
}

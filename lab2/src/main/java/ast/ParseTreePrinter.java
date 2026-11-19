package ast;

/**
 * Друкує дерево синтаксичного розбору як текст з відступами.
 *
 * <p>Працює для дерева будь-якої граматики. Не є частиною конвеєра
 * компіляції: {@code Main} викликає його лише з прапорцем {@code --tree},
 * між розбором і згортанням.
 *
 * <p>Формат: один вузол на рядок, два пробіли відступу на рівень. Нетермінал
 * друкується як {@code Name [code]}; термінал друкується як свій токен,
 * наприклад {@code IDENTIFIER("A")@1:7#0}, де {@code #0} - код із
 * {@link lexer.SymbolTable}.
 */
public class ParseTreePrinter {

    /**
     * Повертає текстову форму всього дерева.
     */
    public String print(ParseNode root) {
        StringBuilder out = new StringBuilder();
        printNode(root, 0, out);
        return out.toString();
    }

    private void printNode(ParseNode node, int depth, StringBuilder out) {
        out.repeat("  ", depth);
        switch (node) {
            case ParseNode.Branch branch -> {
                out.append(branch.symbol().name())
                        .append(" [").append(branch.code()).append("]\n");
                for (ParseNode child : branch.children()) {
                    printNode(child, depth + 1, out);
                }
            }
            case ParseNode.Leaf leaf -> out.append(leaf.token()).append('\n');
        }
    }
}

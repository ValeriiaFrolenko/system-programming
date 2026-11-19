package ast;

/**
 * Друкує дерево синтаксичного розбору у зручному для читання вигляді.
 *
 * <p>Призначений для наочної демонстрації (наприклад, на захисті
 * лабораторної). <b>Не входить</b> у пайплайн компіляції: його викликає
 * лише {@code Main} за прапорцем {@code --tree}, між стадіями
 * {@link parser.Parser} і {@link parser.TreeFolder}.
 *
 * <p>Формат: кожен вузол на своєму рядку, відступ — два пробіли на
 * рівень вкладеності. Нетермінали підписані назвою і кодом у дужках
 * {@code [CODE]}; листки ({@link ParseNode.Name}, {@link ParseNode.NumberLit})
 * показують сам токен — текст, а для імені ще й код із
 * {@link lexer.SymbolTable} (у вигляді {@code #N}).
 */
public class ParseTreePrinter {

    /** Повертає рядкове представлення всього дерева. */
    public String print(ParseNode.Program program) {
        StringBuilder sb = new StringBuilder();
        printNode(program, 0, sb);
        return sb.toString();
    }

    private void printNode(ParseNode node, int depth, StringBuilder sb) {
        indent(depth, sb);

        switch (node) {
            case ParseNode.Program p -> {
                sb.append("Program [").append(ParseNode.Program.CODE).append("]\n");
                printNode(p.sentenceList(), depth + 1, sb);
            }
            case ParseNode.SentenceList sl -> {
                sb.append("SentenceList [").append(ParseNode.SentenceList.CODE).append("]\n");
                for (ParseNode.Sentence s : sl.sentences()) printNode(s, depth + 1, sb);
            }
            case ParseNode.Sentence s -> {
                sb.append("Sentence [").append(ParseNode.Sentence.CODE).append("]\n");
                printNode(s.body(), depth + 1, sb);
            }
            case ParseNode.PointDecl p -> {
                sb.append("PointDecl [").append(ParseNode.PointDecl.CODE).append("]\n");
                printNode(p.name(), depth + 1, sb);
                printNode(p.x(), depth + 1, sb);
                printNode(p.y(), depth + 1, sb);
            }
            case ParseNode.LineThrough l -> {
                sb.append("LineThrough [").append(ParseNode.LineThrough.CODE).append("]\n");
                printNode(l.points(), depth + 1, sb);
            }
            case ParseNode.Build b -> {
                sb.append("Build [").append(ParseNode.Build.CODE).append("]\n");
                printNode(b.kind(), depth + 1, sb);
            }
            case ParseNode.Perpendicular p -> {
                sb.append("Perpendicular [").append(ParseNode.Perpendicular.CODE).append("]\n");
                printNode(p.base(), depth + 1, sb);
                printNode(p.target(), depth + 1, sb);
            }
            case ParseNode.Triangle t -> {
                sb.append("Triangle [").append(ParseNode.Triangle.CODE).append("]\n");
                printNode(t.vertices(), depth + 1, sb);
            }
            case ParseNode.Segment s -> {
                sb.append("Segment [").append(ParseNode.Segment.CODE).append("]\n");
                printNode(s.from(), depth + 1, sb);
                printNode(s.to(), depth + 1, sb);
            }
            case ParseNode.NameList nl -> {
                sb.append("NameList [").append(ParseNode.NameList.CODE).append("]\n");
                for (ParseNode.Name n : nl.names()) printNode(n, depth + 1, sb);
            }
            case ParseNode.NamePair np -> {
                sb.append("NamePair [").append(ParseNode.NamePair.CODE).append("]\n");
                printNode(np.a(), depth + 1, sb);
                printNode(np.b(), depth + 1, sb);
            }
            case ParseNode.Name n -> sb.append("Name [").append(ParseNode.Name.CODE).append("] ")
                    .append(n.token()).append('\n');
            case ParseNode.NumberLit num -> sb.append("NumberLit [").append(ParseNode.NumberLit.CODE).append("] ")
                    .append(num.token()).append('\n');
        }
    }

    private void indent(int depth, StringBuilder sb) {
        sb.repeat("  ", depth);
    }
}

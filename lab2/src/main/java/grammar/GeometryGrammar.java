package grammar;

import lexer.TokenType;

/**
 * Граматика мови геометричних побудов.
 *
 * <p>Це єдине місце в проєкті, де записано конкретну мову. Парсер,
 * побудовник таблиці й принтер дерева приймають {@link Grammar} і не
 * знають, що в ній.
 *
 * <p>Слова у верхньому регістрі, що є типами токенів ({@link TokenType}),
 * - це термінали; інші - нетермінали, оголошені як константи цього класу.
 * Граматика LL(1): три види {@code Build} розрізняються за токеном після
 * {@code BUILD}.
 */
public final class GeometryGrammar {

    public static final NonTerminal PROGRAM = new NonTerminal(0, "Program");
    public static final NonTerminal SENTENCE_LIST = new NonTerminal(1, "SentenceList");
    public static final NonTerminal SENTENCE_LIST_TAIL = new NonTerminal(2, "SentenceListTail");
    public static final NonTerminal SENTENCE = new NonTerminal(3, "Sentence");
    public static final NonTerminal BUILD = new NonTerminal(4, "Build");
    public static final NonTerminal BUILD_KIND = new NonTerminal(5, "BuildKind");
    public static final NonTerminal POINT_DECL = new NonTerminal(6, "PointDecl");
    public static final NonTerminal LINE_THROUGH = new NonTerminal(7, "LineThrough");
    public static final NonTerminal PERPENDICULAR = new NonTerminal(8, "Perpendicular");
    public static final NonTerminal TRIANGLE = new NonTerminal(9, "Triangle");
    public static final NonTerminal SEGMENT = new NonTerminal(10, "Segment");
    public static final NonTerminal NAME_LIST = new NonTerminal(11, "NameList");
    public static final NonTerminal NAME_LIST_TAIL = new NonTerminal(12, "NameListTail");
    public static final NonTerminal NAME_PAIR = new NonTerminal(13, "NamePair");
    public static final NonTerminal NAME = new NonTerminal(14, "Name");
    public static final NonTerminal NUMBER = new NonTerminal(15, "Number");

    private static final Grammar GRAMMAR = build();

    private GeometryGrammar() {
    }

    /**
     * Повертає граматику мови.
     */
    public static Grammar grammar() {
        return GRAMMAR;
    }

    private static Grammar build() {
        return Grammar.builder(PROGRAM)
                .rule(PROGRAM, SENTENCE_LIST, t(TokenType.END))
                .rule(SENTENCE_LIST, SENTENCE, SENTENCE_LIST_TAIL)
                .rule(SENTENCE_LIST_TAIL, t(TokenType.SEMICOLON), SENTENCE_LIST)
                .rule(SENTENCE_LIST_TAIL)

                .rule(SENTENCE, POINT_DECL)
                .rule(SENTENCE, LINE_THROUGH)
                .rule(SENTENCE, BUILD)

                .rule(BUILD, t(TokenType.BUILD), BUILD_KIND)
                .rule(BUILD_KIND, PERPENDICULAR)
                .rule(BUILD_KIND, TRIANGLE)
                .rule(BUILD_KIND, SEGMENT)

                .rule(POINT_DECL, t(TokenType.POINT), NAME, t(TokenType.LPAREN),
                        NUMBER, t(TokenType.COMMA), NUMBER, t(TokenType.RPAREN))
                .rule(LINE_THROUGH, t(TokenType.DRAW), t(TokenType.LINE), t(TokenType.THROUGH), NAME_PAIR)
                .rule(PERPENDICULAR, t(TokenType.PERPENDICULAR), t(TokenType.TO), t(TokenType.LINE),
                        t(TokenType.LPAREN), NAME_PAIR, t(TokenType.RPAREN),
                        t(TokenType.AT), t(TokenType.POINT_LOC), NAME)
                .rule(TRIANGLE, t(TokenType.TRIANGLE), t(TokenType.BY), t(TokenType.THREE),
                        t(TokenType.VERTEX), NAME_LIST)
                .rule(SEGMENT, t(TokenType.SEGMENT), t(TokenType.FROM), NAME, t(TokenType.TO), NAME)

                .rule(NAME_LIST, NAME, NAME_LIST_TAIL)
                .rule(NAME_LIST_TAIL, t(TokenType.COMMA), NAME_LIST)
                .rule(NAME_LIST_TAIL)
                .rule(NAME_PAIR, NAME, t(TokenType.COMMA), NAME)
                .rule(NAME, t(TokenType.IDENTIFIER))
                .rule(NUMBER, t(TokenType.NUMBER))
                .build();
    }

    private static Terminal t(TokenType type) {
        return new Terminal(type);
    }
}

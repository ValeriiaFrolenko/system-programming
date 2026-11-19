package ast;

import lexer.Token;

import java.util.List;

/**
 * Дерево синтаксичного розбору (concrete syntax tree).
 *
 * <p>Кожен record відповідає одному нетерміналу граматики. Дерево будує
 * {@link parser.Parser} методом рекурсивного спуску; воно повторює
 * структуру речення, включно зі службовими вузлами,
 * (наприклад, {@link Sentence} — обгортка над тілом речення).
 *
 * <p>Після побудови дерево згортається у спрощене AST {@link Stmt}
 * класом {@link parser.TreeFolder}.
 *
 * <p>Граматика:
 * <pre>{@code
 * <текст>              ::= <список_речень> #
 * <список_речень>      ::= <речення> <продовження_списку>
 * <продовження_списку> ::= ; <список_речень> | <пусто>
 *
 * <речення>            ::= <точка> | <пряма_через> | <побудова>
 *
 * <побудова>           ::= ПОБУДУВАТИ <вид_побудови>
 * <вид_побудови>       ::= <перпендикуляр> | <трикутник> | <відрізок>
 *
 * <точка>              ::= ТОЧКА <ім'я> ( <число> , <число> )
 * <пряма_через>        ::= ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ <пара_імен>
 * <перпендикуляр>      ::= ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ ( <пара_імен> ) В ТОЧЦІ <ім'я>
 * <трикутник>          ::= ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ <список_імен>
 * <відрізок>           ::= ВІДРІЗОК ВІД <ім'я> ДО <ім'я>
 *
 * <список_імен>        ::= <ім'я> <продовження_імен>
 * <продовження_імен>   ::= , <список_імен> | <пусто>
 * <пара_імен>          ::= <ім'я> , <ім'я>
 * <ім'я>               ::= IDENTIFIER
 * <число>              ::= NUMBER
 * }</pre>
 *
 * <p>Кожен нетермінал має власний числовий код {@link #code()} — для
 * діагностики та наочної демонстрації дерева ({@link ParseTreePrinter}).
 */
public sealed interface ParseNode {

    /** Числовий код нетермінала, унікальний у межах цього sealed-типу. */
    int code();

    /** {@code <вид_побудови>} — диз'юнкція трьох конкретних видів. */
    sealed interface BuildKind extends ParseNode permits Perpendicular, Triangle, Segment {}

    /** {@code <текст>} — корінь дерева: список речень плюс маркер {@code #}. */
    record Program(SentenceList sentenceList) implements ParseNode {
        public static final int CODE = 0;
        public int code() { return CODE; }

        /** Зручність для коду, якому байдуже до CST-вузла {@link SentenceList}. */
        public List<Sentence> sentences() { return sentenceList.sentences(); }
    }

    /** {@code <список_речень>} — послідовність речень верхнього рівня. */
    record SentenceList(List<Sentence> sentences) implements ParseNode {
        public static final int CODE = 1;
        public int code() { return CODE; }
    }

    /** {@code <речення>} — обгортка над тілом одного речення. */
    record Sentence(ParseNode body) implements ParseNode {
        public static final int CODE = 2;
        public int code() { return CODE; }
    }

    /** {@code <точка>} — {@code ТОЧКА A (x, y)}. */
    record PointDecl(Name name, NumberLit x, NumberLit y) implements ParseNode {
        public static final int CODE = 3;
        public int code() { return CODE; }
    }

    /** {@code <пряма_через>} — {@code ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ A, B}. */
    record LineThrough(NamePair points) implements ParseNode {
        public static final int CODE = 4;
        public int code() { return CODE; }
    }

    /** {@code <побудова>} — {@code ПОБУДУВАТИ <вид_побудови>}. */
    record Build(BuildKind kind) implements ParseNode {
        public static final int CODE = 5;
        public int code() { return CODE; }
    }

    /** {@code <перпендикуляр>} — {@code … ДО ПРЯМОЇ (A, B) В ТОЧЦІ C}. */
    record Perpendicular(NamePair base, Name target) implements BuildKind {
        public static final int CODE = 6;
        public int code() { return CODE; }
    }

    /** {@code <трикутник>} — {@code … ПО ТРЬОМ ВЕРШИНАМ A, B, C}. */
    record Triangle(NameList vertices) implements BuildKind {
        public static final int CODE = 7;
        public int code() { return CODE; }
    }

    /** {@code <відрізок>} — {@code … ВІД A ДО B}. */
    record Segment(Name from, Name to) implements BuildKind {
        public static final int CODE = 8;
        public int code() { return CODE; }
    }

    /** {@code <список_імен>} — послідовність імен через кому. */
    record NameList(List<Name> names) implements ParseNode {
        public static final int CODE = 9;
        public int code() { return CODE; }
    }

    /** {@code <пара_імен>} — рівно два імені через кому. */
    record NamePair(Name a, Name b) implements ParseNode {
        public static final int CODE = 10;
        public int code() { return CODE; }
    }

    /** {@code <ім'я>} — листок дерева, несе вихідний токен {@code IDENTIFIER}. */
    record Name(Token token) implements ParseNode {
        public static final int CODE = 11;
        public int code() { return CODE; }
    }

    /** {@code <число>} — листок дерева, несе вихідний токен {@code NUMBER}. */
    record NumberLit(Token token) implements ParseNode {
        public static final int CODE = 12;
        public int code() { return CODE; }
    }
}
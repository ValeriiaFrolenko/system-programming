package parser;

import ast.ParseNode;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Низхідний синтаксичний аналізатор (метод рекурсивного спуску).
 *
 * <p>Будує дерево синтаксичного розбору {@link ParseNode} за граматикою:
 * <pre>{@code
 * <текст>              ::= <список_речень> #
 * <список_речень>      ::= <речення> <продовження_списку>
 * <продовження_списку> ::= ; <список_речень> | <пусто>
 * <речення>            ::= <точка> | <пряма_через> | <побудова>
 * <побудова>           ::= ПОБУДУВАТИ <вид_побудови>
 * <вид_побудови>       ::= <перпендикуляр> | <трикутник> | <відрізок>
 * }</pre>
 *
 * <p>Граматика — строго LL(1): кожне рішення про розгалуження
 * ухвалюється за одним токеном уперед. Три команди починаються зі
 * спільного префікса {@code ПОБУДУВАТИ}; щоб зберегти LL(1), граматику
 * піддано <b>лівій факторизації</b> — префікс винесено в нетермінал
 * {@code <побудова>}, і розгалуження відбувається за наступним токеном
 * ({@code PERPENDICULAR}, {@code TRIANGLE} або {@code SEGMENT}), які
 * вже різні. У коді цьому відповідає {@link #parseBuild()}. Завдяки
 * цьому парсер не робить повернень (backtracking).
 *
 * <p>Після побудови дерево передається в {@link TreeFolder}, який згортає
 * його у спрощене AST {@link ast.Stmt}.
 */
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** Розбирає весь текст і повертає корінь дерева. */
    public ParseNode.Program parse() {
        List<ParseNode.Sentence> sentences = parseSentenceList();
        expect(TokenType.END);
        expect(TokenType.EOF);
        return new ParseNode.Program(sentences);
    }

    /** {@code <список_речень> ::= <речення> <продовження_списку>} */
    private List<ParseNode.Sentence> parseSentenceList() {
        List<ParseNode.Sentence> sentences = new ArrayList<>();
        sentences.add(parseSentence());
        sentences.addAll(parseSentenceListTail());
        return sentences;
    }

    /** {@code <продовження_списку> ::= ; <список_речень> | <пусто>} */
    private List<ParseNode.Sentence> parseSentenceListTail() {
        if (peek().type() == TokenType.SEMICOLON) {
            advance();
            return parseSentenceList();
        }
        return List.of();
    }

    /** {@code <речення>} — розгалуження за першим токеном. */
    private ParseNode.Sentence parseSentence() {
        Token token = peek();
        ParseNode body = switch (token.type()) {
            case POINT -> parsePointDecl();
            case DRAW -> parseLineThrough();
            case BUILD -> parseBuild();
            default -> throw new ParseException(token, "Очікувався початок речення");
        };
        return new ParseNode.Sentence(body);
    }

    /** {@code <точка> ::= ТОЧКА <ім'я> ( <число> , <число> )} */
    private ParseNode.PointDecl parsePointDecl() {
        expect(TokenType.POINT);
        ParseNode.Name name = parseName();
        expect(TokenType.LPAREN);
        ParseNode.NumberLit x = parseNumberLit();
        expect(TokenType.COMMA);
        ParseNode.NumberLit y = parseNumberLit();
        expect(TokenType.RPAREN);
        return new ParseNode.PointDecl(name, x, y);
    }

    /** {@code <пряма_через> ::= ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ <пара_імен>} */
    private ParseNode.LineThrough parseLineThrough() {
        expect(TokenType.DRAW);
        expect(TokenType.LINE);
        expect(TokenType.THROUGH);
        ParseNode.NamePair pair = parseNamePair();
        return new ParseNode.LineThrough(pair);
    }

    /**
     * {@code <побудова> ::= ПОБУДУВАТИ <вид_побудови>}.
     *
     * <p>Це і є ліва факторизація: спільний префікс {@code ПОБУДУВАТИ}
     * споживається тут, і лише після нього розгалуження йде за наступним
     * токеном. Три різні типи {@link TokenType} ({@code PERPENDICULAR},
     * {@code TRIANGLE}, {@code SEGMENT}) утворюють різні FIRST-множини,
     * тому граматика лишається LL(1).
     */
    private ParseNode parseBuild() {
        expect(TokenType.BUILD);
        Token next = peek();

        if (next.type() == TokenType.PERPENDICULAR) return parsePerpendicular();
        if (next.type() == TokenType.TRIANGLE) return parseTriangle();
        if (next.type() == TokenType.SEGMENT) return parseSegment();

        throw new ParseException(next, "Очікувався ПЕРПЕНДИКУЛЯР, ТРИКУТНИК або ВІДРІЗОК");
    }

    /** {@code <перпендикуляр> ::= ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ ( <пара_імен> ) В ТОЧЦІ <ім'я>} */
    private ParseNode.Perpendicular parsePerpendicular() {
        expect(TokenType.PERPENDICULAR);
        expect(TokenType.TO);
        expect(TokenType.LINE);
        expect(TokenType.LPAREN);
        ParseNode.NamePair base = parseNamePair();
        expect(TokenType.RPAREN);
        expect(TokenType.AT);
        expect(TokenType.POINT_LOC);
        ParseNode.Name target = parseName();
        return new ParseNode.Perpendicular(base, target);
    }

    /** {@code <трикутник> ::= ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ <список_імен>} */
    private ParseNode.Triangle parseTriangle() {
        expect(TokenType.TRIANGLE);
        expect(TokenType.BY);
        expect(TokenType.THREE);
        expect(TokenType.VERTEX);
        ParseNode.NameList vertices = parseNameList();
        return new ParseNode.Triangle(vertices);
    }

    /** {@code <відрізок> ::= ВІДРІЗОК ВІД <ім'я> ДО <ім'я>} */
    private ParseNode.Segment parseSegment() {
        expect(TokenType.SEGMENT);
        expect(TokenType.FROM);
        ParseNode.Name from = parseName();
        expect(TokenType.TO);
        ParseNode.Name to = parseName();
        return new ParseNode.Segment(from, to);
    }

    /** {@code <список_імен> ::= <ім'я> <продовження_імен>} */
    private ParseNode.NameList parseNameList() {
        List<ParseNode.Name> names = new ArrayList<>();
        names.add(parseName());
        names.addAll(parseNameListTail());
        return new ParseNode.NameList(names);
    }

    /** {@code <продовження_імен> ::= , <список_імен> | <пусто>} */
    private List<ParseNode.Name> parseNameListTail() {
        if (peek().type() == TokenType.COMMA) {
            advance();
            return parseNameList().names();
        }
        return List.of();
    }

    /** {@code <пара_імен> ::= <ім'я> , <ім'я>} (кома — наше свідоме відхилення від методички). */
    private ParseNode.NamePair parseNamePair() {
        ParseNode.Name a = parseName();
        expect(TokenType.COMMA);
        ParseNode.Name b = parseName();
        return new ParseNode.NamePair(a, b);
    }

    /** {@code <ім'я> ::= IDENTIFIER} */
    private ParseNode.Name parseName() {
        Token token = expect(TokenType.IDENTIFIER);
        return new ParseNode.Name(token);
    }

    /** {@code <число> ::= NUMBER} */
    private ParseNode.NumberLit parseNumberLit() {
        Token token = expect(TokenType.NUMBER);
        return new ParseNode.NumberLit(token);
    }

    private Token peek() { return tokens.get(pos); }

    private Token advance() {
        Token token = tokens.get(pos);
        pos++;
        return token;
    }

    private Token expect(TokenType type) {
        Token token = peek();
        if (token.type() != type) {
            throw new ParseException(token, "Очікувалося " + type);
        }
        pos++;
        return token;
    }
}
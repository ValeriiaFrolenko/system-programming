package vfrolenko;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexerTest {

    // Surname letters allowed in identifiers: F r o l e n k o (upper and lower case)
    private static final String SURNAME = "\u0424\u0440\u043e\u043b\u0435\u043d\u043a\u043e";
    private static final String SURNAME_LOWER = "\u0444\u0440\u043e\u043b\u0435\u043d\u043a\u043e";
    private static final String ALLOWED_CYRILLIC =
            "\u0424\u0444\u0420\u0440\u041e\u043e\u041b\u043b\u0415\u0435\u041d\u043d\u041a\u043a";
    // Cyrillic letters that are NOT part of the surname
    private static final String FIRST_NAME = "\u0412\u0430\u043b\u0435\u0440\u0456\u044f";
    private static final String[] FORBIDDEN_CYRILLIC = {"\u0412", "\u0430", "\u0456", "\u044f"};

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static List<Token> lex(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            Token token = lexer.nextToken();
            if (token.type() == TokenType.EOF) {
                return result;
            }
            result.add(token);
        }
        throw new AssertionError("lexer did not reach EOF for input: " + source);
    }

    private static Token single(String source) {
        List<Token> tokens = lex(source);
        assertEquals(1, tokens.size(), () -> "expected exactly one token but got " + tokens);
        return tokens.get(0);
    }

    private static List<TokenType> types(String source) {
        return lex(source).stream().map(Token::type).toList();
    }

    private static void assertToken(Token token, TokenType type, String value, int line, int column) {
        assertEquals(type, token.type(), "type of " + token);
        assertEquals(value, token.value(), "value of " + token);
        assertEquals(line, token.line(), "line of " + token);
        assertEquals(column, token.column(), "column of " + token);
    }

    // ------------------------------------------------------------------
    // EOF and whitespace
    // ------------------------------------------------------------------

    @Test
    void emptyInputYieldsEof() {
        assertToken(new Lexer("").nextToken(), TokenType.EOF, "", 1, 1);
    }

    @Test
    void whitespaceOnlyInputYieldsEofAtEndPosition() {
        assertToken(new Lexer("  \n  ").nextToken(), TokenType.EOF, "", 2, 3);
    }

    @Test
    void eofIsReturnedRepeatedly() {
        Lexer lexer = new Lexer("x");
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
    }

    @Test
    void eofPositionIsAfterLastToken() {
        Lexer lexer = new Lexer("int x");
        lexer.nextToken();
        lexer.nextToken();
        assertToken(lexer.nextToken(), TokenType.EOF, "", 1, 6);
    }

    @Test
    void whitespaceBetweenTokensIsSkipped() {
        assertEquals(List.of(TokenType.KEYWORD, TokenType.IDENTIFIER),
                types("  \t int \r\n\n   x  "));
    }

    // ------------------------------------------------------------------
    // identifiers
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"a", "x", "Z", "result", "x1", "abc123", "aB", "Z9z", "name", "bad1",
            "INT", "If", "Sin", "iff", "integer", "sinx", "returns", "elsewhere", "floats"})
    void latinIdentifiers(String word) {
        Token token = single(word);
        assertEquals(TokenType.IDENTIFIER, token.type());
        assertEquals(word, token.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {SURNAME, SURNAME_LOWER, "a" + SURNAME, SURNAME + "1",
            "x" + SURNAME_LOWER + "9", "a1" + SURNAME + "2b"})
    void identifiersWithSurnameLetters(String word) {
        Token token = single(word);
        assertEquals(TokenType.IDENTIFIER, token.type());
        assertEquals(word, token.value());
    }

    @Test
    void everyAllowedCyrillicLetterIsAnIdentifier() {
        for (char c : ALLOWED_CYRILLIC.toCharArray()) {
            Token token = single(String.valueOf(c));
            assertEquals(TokenType.IDENTIFIER, token.type(), "letter " + (int) c);
        }
    }

    @Test
    void forbiddenCyrillicLettersAreErrors() {
        for (String letter : FORBIDDEN_CYRILLIC) {
            Token token = single(letter);
            assertEquals(TokenType.ERROR, token.type());
            assertEquals("Unknown character: " + letter, token.value());
        }
    }

    @Test
    void nonCyrillicNonAsciiLetterIsError() {
        Token token = single("\u00e9");
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Unknown character: \u00e9", token.value());
    }

    @Test
    void forbiddenLetterInsideWordSplitsIdentifier() {
        List<Token> tokens = lex("a\u0412b");
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.ERROR, TokenType.IDENTIFIER),
                tokens.stream().map(Token::type).toList());
        assertEquals("a", tokens.get(0).value());
        assertEquals("b", tokens.get(2).value());
    }

    @Test
    void identifierIsTerminatedByDelimiter() {
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.SEMICOLON), types("abc;"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.COMMA, TokenType.IDENTIFIER), types("a,b"));
    }

    @Test
    void underscoreIsNotAllowedInIdentifiers() {
        List<Token> tokens = lex("_x");
        assertEquals(List.of(TokenType.ERROR, TokenType.IDENTIFIER),
                tokens.stream().map(Token::type).toList());
        assertEquals("Unknown character: _", tokens.get(0).value());
        assertEquals("x", tokens.get(1).value());
    }

    // ------------------------------------------------------------------
    // keywords and built-in functions
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"if", "else", "while", "for", "return", "true", "false",
            "int", "float", "string", "void", "do", "break", "continue"})
    void keywords(String word) {
        Token token = single(word);
        assertEquals(TokenType.KEYWORD, token.type());
        assertEquals(word, token.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin", "cos", "tan", "sqrt", "abs", "log", "exp", "pow",
            "floor", "ceil", "round", "min", "max"})
    void builtinFunctions(String word) {
        Token token = single(word);
        assertEquals(TokenType.BUILTIN_FUNCTION, token.type());
        assertEquals(word, token.value());
    }

    @Test
    void keywordsAreCaseSensitive() {
        assertEquals(TokenType.IDENTIFIER, single("IF").type());
        assertEquals(TokenType.IDENTIFIER, single("While").type());
        assertEquals(TokenType.IDENTIFIER, single("SQRT").type());
    }

    @Test
    void keywordFollowedByParenthesis() {
        assertEquals(List.of(TokenType.KEYWORD, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.RPAREN),
                types("if(x)"));
    }

    @Test
    void builtinCall() {
        assertEquals(List.of(TokenType.BUILTIN_FUNCTION, TokenType.LPAREN, TokenType.FLOAT, TokenType.RPAREN),
                types("sqrt(2.5)"));
    }

    // ------------------------------------------------------------------
    // numbers
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "42", "100", "123456789"})
    void integers(String literal) {
        Token token = single(literal);
        assertEquals(TokenType.INTEGER, token.type());
        assertEquals(literal, token.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.14", "0.0", "0.5", "10.25", "100.001"})
    void floats(String literal) {
        Token token = single(literal);
        assertEquals(TokenType.FLOAT, token.type());
        assertEquals(literal, token.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.14.5", "3.14.5.6", "123abc", "3.14abc", "1x", "0a", "9abc9"})
    void malformedNumbersAreSingleErrorToken(String literal) {
        Token token = single(literal);
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Invalid number literal: " + literal, token.value());
    }

    @Test
    void digitFollowedByAllowedCyrillicLetterIsInvalidNumber() {
        Token token = single("1\u0444");
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Invalid number literal: 1\u0444", token.value());
    }

    @Test
    void trailingDotAfterIntegerIsNotPartOfNumber() {
        List<Token> tokens = lex("5.");
        assertEquals(List.of(TokenType.INTEGER, TokenType.ERROR),
                tokens.stream().map(Token::type).toList());
        assertEquals("5", tokens.get(0).value());
        assertEquals("Unknown character: .", tokens.get(1).value());
    }

    @Test
    void leadingDotIsNotPartOfNumber() {
        List<Token> tokens = lex(".5");
        assertEquals(List.of(TokenType.ERROR, TokenType.INTEGER),
                tokens.stream().map(Token::type).toList());
        assertEquals("5", tokens.get(1).value());
    }

    @Test
    void negativeNumberIsMinusFollowedByNumber() {
        assertEquals(List.of(TokenType.MINUS, TokenType.INTEGER), types("-5"));
        assertEquals(List.of(TokenType.MINUS, TokenType.FLOAT), types("-5.5"));
    }

    @Test
    void errorNumberDoesNotSwallowFollowingDelimiter() {
        assertEquals(List.of(TokenType.ERROR, TokenType.SEMICOLON), types("3.14.5;"));
        assertEquals(List.of(TokenType.ERROR, TokenType.SEMICOLON), types("123abc;"));
    }

    @Test
    void numbersSeparatedByWhitespace() {
        assertEquals(List.of(TokenType.INTEGER, TokenType.INTEGER), types("12 34"));
    }

    @Test
    void unicodeDigitIsNotANumber() {
        Token token = single("\u0663");
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Unknown character: \u0663", token.value());
    }

    @Test
    void unicodeDigitAfterAsciiDigitIsNotPartOfNumber() {
        assertEquals(List.of(TokenType.INTEGER, TokenType.ERROR), types("1\u0663"));
    }

    // ------------------------------------------------------------------
    // operators and delimiters
    // ------------------------------------------------------------------

    static Stream<Arguments> operators() {
        return Stream.of(
                Arguments.of("+", TokenType.PLUS),
                Arguments.of("-", TokenType.MINUS),
                Arguments.of("*", TokenType.MULTIPLY),
                Arguments.of("/", TokenType.DIVIDE),
                Arguments.of("==", TokenType.EQUAL),
                Arguments.of("!=", TokenType.NOT_EQUAL),
                Arguments.of("<", TokenType.LESS),
                Arguments.of(">", TokenType.GREATER),
                Arguments.of("<=", TokenType.LESS_EQUAL),
                Arguments.of(">=", TokenType.GREATER_EQUAL),
                Arguments.of("=", TokenType.ASSIGN),
                Arguments.of("(", TokenType.LPAREN),
                Arguments.of(")", TokenType.RPAREN),
                Arguments.of("{", TokenType.LBRACE),
                Arguments.of("}", TokenType.RBRACE),
                Arguments.of(";", TokenType.SEMICOLON),
                Arguments.of(",", TokenType.COMMA)
        );
    }

    @ParameterizedTest
    @MethodSource("operators")
    void operatorsAndDelimiters(String text, TokenType type) {
        Token token = single(text);
        assertEquals(type, token.type());
        assertEquals(text, token.value());
    }

    @Test
    void longestMatchForTwoCharOperators() {
        assertEquals(List.of(TokenType.EQUAL, TokenType.ASSIGN), types("==="));
        assertEquals(List.of(TokenType.NOT_EQUAL, TokenType.ASSIGN), types("!=="));
        assertEquals(List.of(TokenType.LESS_EQUAL, TokenType.GREATER), types("<=>"));
        assertEquals(List.of(TokenType.LESS, TokenType.GREATER), types("<>"));
        assertEquals(List.of(TokenType.GREATER_EQUAL, TokenType.LESS), types(">=<"));
        assertEquals(List.of(TokenType.ASSIGN, TokenType.GREATER), types("=>"));
    }

    @Test
    void operatorsSeparatedByWhitespaceAreNotMerged() {
        assertEquals(List.of(TokenType.ASSIGN, TokenType.ASSIGN), types("= ="));
        assertEquals(List.of(TokenType.LESS, TokenType.ASSIGN), types("< ="));
        assertEquals(List.of(TokenType.DIVIDE, TokenType.DIVIDE), types("/ /"));
    }

    @Test
    void loneExclamationMarkIsError() {
        Token token = single("!");
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Unknown character: !", token.value());
        assertEquals(List.of(TokenType.ERROR, TokenType.IDENTIFIER), types("!x"));
        assertEquals(List.of(TokenType.ERROR, TokenType.ASSIGN), types("! ="));
    }

    @Test
    void divisionIsNotComment() {
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.DIVIDE, TokenType.IDENTIFIER), types("a/b"));
        assertEquals(TokenType.DIVIDE, single("/").type());
    }

    @Test
    void arithmeticExpressionWithoutSpaces() {
        assertEquals(List.of(TokenType.INTEGER, TokenType.PLUS, TokenType.IDENTIFIER,
                        TokenType.MULTIPLY, TokenType.FLOAT, TokenType.MINUS, TokenType.INTEGER,
                        TokenType.DIVIDE, TokenType.INTEGER),
                types("1+x*2.5-3/4"));
    }

    @Test
    void assignmentWithNegativeValue() {
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.MINUS, TokenType.INTEGER),
                types("x=-1"));
    }

    // ------------------------------------------------------------------
    // unknown characters
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"@", "#", "$", "&", "|", "%", "^", "~", "?", ":", "[", "]", "'", "`", "\\", ".", "_"})
    void unknownCharacterIsError(String text) {
        Token token = single(text);
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Unknown character: " + text, token.value());
    }

    @Test
    void controlCharacterIsError() {
        assertEquals(TokenType.ERROR, single("\0").type());
    }

    @Test
    void errorDoesNotStopLexing() {
        assertEquals(List.of(TokenType.KEYWORD, TokenType.IDENTIFIER, TokenType.ASSIGN,
                        TokenType.ERROR, TokenType.SEMICOLON),
                types("int bad = @;"));
        assertEquals(List.of(TokenType.IDENTIFIER, TokenType.ERROR, TokenType.IDENTIFIER), types("a@b"));
        assertEquals(List.of(TokenType.ERROR, TokenType.ERROR), types("@@"));
    }

    // ------------------------------------------------------------------
    // strings
    // ------------------------------------------------------------------

    @Test
    void simpleString() {
        assertToken(new Lexer("\"hello\"").nextToken(), TokenType.STRING, "hello", 1, 1);
    }

    @Test
    void emptyString() {
        assertToken(new Lexer("\"\"").nextToken(), TokenType.STRING, "", 1, 1);
    }

    @Test
    void stringWithSpacesAndUkrainianText() {
        Token token = single("\"" + FIRST_NAME + " " + SURNAME + "\"");
        assertEquals(TokenType.STRING, token.type());
        assertEquals(FIRST_NAME + " " + SURNAME, token.value());
    }

    @Test
    void stringContentIsNotTokenized() {
        assertEquals(TokenType.STRING, single("\"if x = 1; @ 3.14.5\"").type());
        assertEquals(TokenType.STRING, single("\"a//b\"").type());
    }

    @Test
    void stringEscapes() {
        assertEquals("a\nb", single("\"a\\nb\"").value());
        assertEquals("a\tb", single("\"a\\tb\"").value());
        assertEquals("say \"hi\"", single("\"say \\\"hi\\\"\"").value());
        assertEquals("a\\b", single("\"a\\\\b\"").value());
        assertEquals("a\\qb", single("\"a\\qb\"").value());
    }

    @Test
    void escapedQuoteDoesNotTerminateString() {
        Token token = single("\"a\\\"b\"");
        assertEquals(TokenType.STRING, token.type());
        assertEquals("a\"b", token.value());
    }

    @Test
    void stringFollowedByTokens() {
        List<Token> tokens = lex("\"abc\" x");
        assertToken(tokens.get(0), TokenType.STRING, "abc", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 1, 7);
    }

    @Test
    void unterminatedStringAtEndOfInput() {
        assertToken(new Lexer("\"abc").nextToken(), TokenType.ERROR,
                "Unterminated string literal: \"abc", 1, 1);
    }

    @Test
    void loneQuoteIsUnterminatedString() {
        assertToken(new Lexer("\"").nextToken(), TokenType.ERROR,
                "Unterminated string literal: \"", 1, 1);
    }

    @Test
    void unterminatedStringEndsAtLineBreak() {
        List<Token> tokens = lex("\"abc\nint x");
        assertToken(tokens.get(0), TokenType.ERROR, "Unterminated string literal: \"abc", 1, 1);
        assertToken(tokens.get(1), TokenType.KEYWORD, "int", 2, 1);
        assertToken(tokens.get(2), TokenType.IDENTIFIER, "x", 2, 5);
    }

    @Test
    void unterminatedStringEndsAtCrLf() {
        List<Token> tokens = lex("\"abc\r\nint");
        assertToken(tokens.get(0), TokenType.ERROR, "Unterminated string literal: \"abc", 1, 1);
        assertToken(tokens.get(1), TokenType.KEYWORD, "int", 2, 1);
    }

    @Test
    void backslashAtEndOfInputInsideString() {
        Token token = single("\"abc\\");
        assertEquals(TokenType.ERROR, token.type());
        assertEquals("Unterminated string literal: \"abc\\", token.value());
    }

    @Test
    void backslashBeforeLineBreakDoesNotContinueString() {
        List<Token> tokens = lex("\"a\\\nb");
        assertEquals(TokenType.ERROR, tokens.get(0).type());
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "b", 2, 1);
    }

    @Test
    void linesAreCountedAfterString() {
        List<Token> tokens = lex("\"abc\"\nx");
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 2, 1);
    }

    // ------------------------------------------------------------------
    // comments
    // ------------------------------------------------------------------

    @Test
    void lineComment() {
        assertToken(new Lexer("// text").nextToken(), TokenType.COMMENT, "// text", 1, 1);
    }

    @Test
    void emptyCommentAtEndOfInputDoesNotThrow() {
        assertDoesNotThrow(() -> lex("//"));
        assertToken(new Lexer("//").nextToken(), TokenType.COMMENT, "//", 1, 1);
    }

    @Test
    void emptyCommentDoesNotSwallowNextLine() {
        List<Token> tokens = lex("//\nint y = 1;");
        assertEquals(List.of(TokenType.COMMENT, TokenType.KEYWORD, TokenType.IDENTIFIER,
                        TokenType.ASSIGN, TokenType.INTEGER, TokenType.SEMICOLON),
                tokens.stream().map(Token::type).toList());
        assertToken(tokens.get(0), TokenType.COMMENT, "//", 1, 1);
        assertToken(tokens.get(1), TokenType.KEYWORD, "int", 2, 1);
    }

    @Test
    void emptyCommentBeforeCrLfDoesNotSwallowNextLine() {
        List<Token> tokens = lex("//\r\nint");
        assertToken(tokens.get(0), TokenType.COMMENT, "//", 1, 1);
        assertToken(tokens.get(1), TokenType.KEYWORD, "int", 2, 1);
    }

    @Test
    void commentValueExcludesCarriageReturn() {
        List<Token> tokens = lex("// a\r\nb");
        assertToken(tokens.get(0), TokenType.COMMENT, "// a", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "b", 2, 1);
    }

    @Test
    void commentAfterCode() {
        List<Token> tokens = lex("x // c");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "x", 1, 1);
        assertToken(tokens.get(1), TokenType.COMMENT, "// c", 1, 3);
    }

    @Test
    void commentContentIsNotTokenized() {
        assertEquals(List.of(TokenType.COMMENT), types("// int x = 1; @ 3.14.5 \"unterminated"));
        assertEquals(List.of(TokenType.COMMENT), types("// " + FIRST_NAME + " " + SURNAME));
    }

    @Test
    void multipleSlashesFormOneComment() {
        assertToken(new Lexer("//// x").nextToken(), TokenType.COMMENT, "//// x", 1, 1);
        assertToken(new Lexer("///").nextToken(), TokenType.COMMENT, "///", 1, 1);
    }

    @Test
    void commentEndsAtLineBreak() {
        List<Token> tokens = lex("// a\nb");
        assertToken(tokens.get(0), TokenType.COMMENT, "// a", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "b", 2, 1);
    }

    // ------------------------------------------------------------------
    // positions
    // ------------------------------------------------------------------

    @Test
    void columnsOnSingleLine() {
        List<Token> tokens = lex("int x = 1;");
        assertToken(tokens.get(0), TokenType.KEYWORD, "int", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 1, 5);
        assertToken(tokens.get(2), TokenType.ASSIGN, "=", 1, 7);
        assertToken(tokens.get(3), TokenType.INTEGER, "1", 1, 9);
        assertToken(tokens.get(4), TokenType.SEMICOLON, ";", 1, 10);
    }

    @Test
    void positionsAcrossLines() {
        List<Token> tokens = lex("a\n  b\n\nc");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "a", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "b", 2, 3);
        assertToken(tokens.get(2), TokenType.IDENTIFIER, "c", 4, 1);
    }

    @Test
    void tabCountsAsOneColumn() {
        assertToken(new Lexer("\tx").nextToken(), TokenType.IDENTIFIER, "x", 1, 2);
    }

    @Test
    void crLfCountsAsSingleLineBreak() {
        List<Token> tokens = lex("a\r\nb");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "a", 1, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "b", 2, 1);
    }

    @Test
    void twoCharOperatorPosition() {
        List<Token> tokens = lex("a == b");
        assertToken(tokens.get(1), TokenType.EQUAL, "==", 1, 3);
        assertToken(tokens.get(2), TokenType.IDENTIFIER, "b", 1, 6);
    }

    @Test
    void errorTokenPosition() {
        assertToken(lex("x @").get(1), TokenType.ERROR, "Unknown character: @", 1, 3);
    }

    // ------------------------------------------------------------------
    // robustness: every input must terminate without exceptions
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\r\n", "/", "//", "///", "\"", "\"\\", "\\", "3.", ".", "..",
            "!", "=", "<", ">", "123abc", "3.14.5", "3.14.", "1.2.", "\"abc", "\"abc\\", "@", "\0",
            "a\u0412", "\u0412a", "1\u0412", "//\n", "//\r\n", "x//", "\"\"\"", "'", "0.0.0.0"})
    void lexerTerminatesOnTrickyInput(String source) {
        assertDoesNotThrow(() -> lex(source));
    }

    // ------------------------------------------------------------------
    // full program from the lab
    // ------------------------------------------------------------------

    private static String labProgram(String newline) {
        return String.join(newline,
                "// lab lexer demo",
                "int " + SURNAME + " = 42;",
                "float result = 3.14 * " + SURNAME + ";",
                "",
                "if (result > 100) {",
                "    result = sqrt(result) + cos(0.0);",
                "} else {",
                "    result = result - 1;",
                "}",
                "",
                "while (" + SURNAME + " != 0) {",
                "    " + SURNAME + " = " + SURNAME + " - 1;",
                "}",
                "",
                "string name = \"" + FIRST_NAME + " " + SURNAME + "\";",
                "",
                "float x = sin(3.14) + log(result) * abs(-5);",
                "",
                "float bad1 = 3.14.5;",
                "int bad2 = 123abc;",
                "int bad3 = @;");
    }

    private static void assertLabProgram(String source) {
        List<Token> tokens = lex(source);
        assertEquals(95, tokens.size());

        List<Token> errors = tokens.stream().filter(t -> t.type() == TokenType.ERROR).toList();
        assertEquals(3, errors.size());
        assertToken(errors.get(0), TokenType.ERROR, "Invalid number literal: 3.14.5", 19, 14);
        assertToken(errors.get(1), TokenType.ERROR, "Invalid number literal: 123abc", 20, 12);
        assertToken(errors.get(2), TokenType.ERROR, "Unknown character: @", 21, 12);

        assertToken(tokens.get(0), TokenType.COMMENT, "// lab lexer demo", 1, 1);
        assertToken(tokens.get(2), TokenType.IDENTIFIER, SURNAME, 2, 5);

        Token string = tokens.stream().filter(t -> t.type() == TokenType.STRING).findFirst().orElseThrow();
        assertToken(string, TokenType.STRING, FIRST_NAME + " " + SURNAME, 15, 15);

        assertToken(tokens.get(tokens.size() - 1), TokenType.SEMICOLON, ";", 21, 13);
    }

    @Test
    void labProgramWithLf() {
        assertLabProgram(labProgram("\n"));
    }

    @Test
    void labProgramWithCrLf() {
        assertLabProgram(labProgram("\r\n"));
    }

    @Test
    void labProgramContainsAllRequiredTokenClasses() {
        List<TokenType> found = types(labProgram("\n"));
        assertTrue(found.containsAll(List.of(TokenType.IDENTIFIER, TokenType.INTEGER, TokenType.FLOAT,
                TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.BUILTIN_FUNCTION,
                TokenType.KEYWORD, TokenType.ERROR)));
    }
}
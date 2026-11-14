package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Лексичний аналізатор.
 *
 * <p>Перетворює вихідний текст програми на послідовність токенів
 * ({@link Token}). Розпізнає:
 * <ul>
 *   <li>ключові слова — за <b>основами</b> (див. {@link #STEM_WORDS}),
 *       що дозволяє приймати будь-які відмінкові форми без окремого
 *       переліку закінчень: {@code ТОЧКА}, {@code ТОЧКИ}, {@code ТОЧОК} →
 *       {@link TokenType#POINT};</li>
 *   <li>точні слова ({@link #EXACT_WORDS}) — для коротких прийменників
 *       {@code В}, {@code ДО}, {@code ПО}, {@code ВІД}, {@code ЧЕРЕЗ},
 *       які небезпечно матчити за основою (основа {@code В} збіглася б із
 *       {@code ВЕРШИНАМ});</li>
 *   <li>числа — цілі й дробові, з опціональним знаком {@code +}/{@code -};</li>
 *   <li>ідентифікатори — лише латиниця, реєстронезалежно; код береться
 *       з {@link SymbolTable};</li>
 *   <li>пунктуацію {@code ( ) , ; #}.</li>
 * </ul>
 *
 * <p>Кінець тексту — символ {@code #}. Останнім у потоці завжди йде
 * {@link TokenType#EOF}.
 *
 * <p>Порядок перевірки основ важливий: {@code ПЕРПЕНДИКУЛЯР} перевіряється
 * раніше за {@code ПРЯМ}, {@code ТОЧК} — після {@code ТОЧЦ}, інакше
 * {@code ТОЧЦІ} розпізналося б як {@code ТОЧК} + залишок. Це досягається
 * порядком вставки в {@link LinkedHashMap}.
 */
public class Lexer {
    private static final Map<String, TokenType> EXACT_WORDS = new HashMap<>();
    private static final Map<String, TokenType> STEM_WORDS = new LinkedHashMap<>();

    static {
        EXACT_WORDS.put("В", TokenType.AT);
        EXACT_WORDS.put("ДО", TokenType.TO);
        EXACT_WORDS.put("ПО", TokenType.BY);
        EXACT_WORDS.put("ВІД", TokenType.FROM);
        EXACT_WORDS.put("ЧЕРЕЗ", TokenType.THROUGH);

        STEM_WORDS.put("ПЕРПЕНДИКУЛЯР", TokenType.PERPENDICULAR);
        STEM_WORDS.put("ТРИКУТНИК", TokenType.TRIANGLE);
        STEM_WORDS.put("ПОБУДУВА", TokenType.BUILD);
        STEM_WORDS.put("ПРОВЕСТ", TokenType.DRAW);
        STEM_WORDS.put("ВЕРШИН", TokenType.VERTEX);
        STEM_WORDS.put("ТРЬОМ", TokenType.THREE);
        STEM_WORDS.put("ТОЧЦ", TokenType.POINT_LOC);
        STEM_WORDS.put("ТОЧК", TokenType.POINT);
        STEM_WORDS.put("ПРЯМ", TokenType.LINE);
        STEM_WORDS.put("ВІДРІЗ", TokenType.SEGMENT);
    }

    private final String source;
    private final SymbolTable symbolTable;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    /** Створює лексер із власною таблицею ідентифікаторів. */
    public Lexer(String source) {
        this(source, new SymbolTable());
    }

    /** Створює лексер із зовнішньою таблицею (для повторного використання). */
    public Lexer(String source, SymbolTable symbolTable) {
        this.source = source;
        this.symbolTable = symbolTable;
    }

    public SymbolTable symbolTable() { return symbolTable; }

    /** Повертає всі токени тексту, завершуючи потоком {@link TokenType#EOF}. */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            if (isAtEnd()) {
                tokens.add(new Token(TokenType.EOF, "", line, column));
                return tokens;
            }
            tokens.add(nextToken());
        }
    }

    /** Розпізнає один токен з поточної позиції (без пропусків). */
    private Token nextToken() {
        int startLine = line;
        int startCol = column;
        char c = peek();

        if (c == '(') { advance(); return new Token(TokenType.LPAREN, "(", startLine, startCol); }
        if (c == ')') { advance(); return new Token(TokenType.RPAREN, ")", startLine, startCol); }
        if (c == ',') { advance(); return new Token(TokenType.COMMA, ",", startLine, startCol); }
        if (c == ';') { advance(); return new Token(TokenType.SEMICOLON, ";", startLine, startCol); }
        if (c == '#') { advance(); return new Token(TokenType.END, "#", startLine, startCol); }
        if (Character.isDigit(c) || c == '-' || c == '+') return readNumber();
        if (Character.isLetter(c)) return readWordOrIdentifier();

        throw new LexException(startLine, startCol, "Неочікуваний символ: '" + c + "'");
    }

    /** Читає число: {@code [+-]?digit+(\.digit+)?}. */
    private Token readNumber() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        if (peek() == '-' || peek() == '+') advance();
        if (!Character.isDigit(peek())) throw new LexException(startLine, startCol, "Очікувалась цифра");

        while (Character.isDigit(peek())) advance();

        if (peek() == '.') {
            advance();
            if (!Character.isDigit(peek())) throw new LexException(startLine, startCol, "Очікувалась цифра після десяткової точки");
            while (Character.isDigit(peek())) advance();
        }

        return new Token(TokenType.NUMBER, source.substring(start, pos), startLine, startCol);
    }

    /** Читає або ключове слово (за основою), або ідентифікатор (латиниця). */
    private Token readWordOrIdentifier() {
        int startLine = line;
        int startCol = column;
        int start = pos;

        while (!isAtEnd() && Character.isLetter(peek())) advance();

        String text = source.substring(start, pos);
        String upper = text.toUpperCase(Locale.ROOT);

        if (EXACT_WORDS.containsKey(upper)) {
            return new Token(EXACT_WORDS.get(upper), text, startLine, startCol);
        }

        for (Map.Entry<String, TokenType> entry : STEM_WORDS.entrySet()) {
            String stem = entry.getKey();
            if (upper.startsWith(stem)) {
                return new Token(entry.getValue(), text, startLine, startCol);
            }
        }

        if (isLatin(text)) {
            int code = symbolTable.codeFor(upper);
            return new Token(TokenType.IDENTIFIER, text, startLine, startCol, code);
        }

        throw new LexException(startLine, startCol, "Невідоме слово: '" + text + "'");
    }

    /** Чи складається рядок лише з латинських літер (обидва регістри). */
    private static boolean isLatin(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) return false;
        }
        return true;
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) advance();
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private void advance() {
        if (isAtEnd()) return;
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else if (c == '\r') {
            if (!isAtEnd() && source.charAt(pos) == '\n') pos++;
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }
}

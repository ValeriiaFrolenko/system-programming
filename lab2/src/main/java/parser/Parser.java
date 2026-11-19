package parser;

import ast.ParseNode;
import grammar.FirstFollowSets;
import grammar.Grammar;
import grammar.GrammarException;
import grammar.LL1Table;
import grammar.NonTerminal;
import grammar.Production;
import grammar.Symbol;
import grammar.Terminal;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Табличний LL(1)-парсер, що працює для будь-якої {@link Grammar}.
 *
 * <p>Парсер не містить жодного правила конкретної мови. Під час побудови
 * він обчислює множини FIRST і FOLLOW та {@link LL1Table} з переданої йому
 * граматики. Далі {@link #parse(List)} виконує один цикл над стеком
 * символів граматики:
 * <ul>
 *   <li>термінал на вершині стека має збігатися з поточним токеном,
 *       і токен споживається;</li>
 *   <li>нетермінал на вершині замінюється правою частиною продукції,
 *       яку таблиця дає для поточного токена.</li>
 * </ul>
 * Жодних повернень (backtracking) немає. Щоб змінити мову, змініть
 * граматику; цей клас лишається тим самим.
 *
 * <p>Дерево розбору будується під час циклу: кожен запис у стеку пам'ятає
 * список дітей вузла, до якого належить.
 */
public final class Parser {
    private final Grammar grammar;
    private final LL1Table table;

    /**
     * Створює парсер для граматики.
     *
     * @throws GrammarException якщо граматика не є LL(1)
     */
    public Parser(Grammar grammar) {
        this.grammar = grammar;
        this.table = new LL1Table(grammar, new FirstFollowSets(grammar));
    }

    /**
     * Розбирає список токенів, що закінчується {@link TokenType#EOF}.
     *
     * @return корінь дерева розбору, {@link ParseNode.Branch} стартового символу
     * @throws ParseException якщо токени не належать мові
     */
    public ParseNode parse(List<Token> tokens) {
        List<ParseNode> roots = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(new Terminal(TokenType.EOF), roots));
        stack.push(new Frame(grammar.start(), roots));

        int position = 0;
        while (!stack.isEmpty()) {
            if (position >= tokens.size()) {
                throw new IllegalArgumentException("Token list must end with " + TokenType.EOF);
            }
            Frame frame = stack.pop();
            Token token = tokens.get(position);
            switch (frame.symbol()) {
                case Terminal terminal -> {
                    match(terminal, token, frame.siblings());
                    position++;
                }
                case NonTerminal nonTerminal -> expand(nonTerminal, token, frame.siblings(), stack);
            }
        }
        return roots.getFirst();
    }

    private static void match(Terminal expected, Token actual, List<ParseNode> siblings) {
        if (actual.type() != expected.type()) {
            throw new ParseException(actual, "Expected " + expected.type());
        }
        if (expected.type() != TokenType.EOF) {
            siblings.add(ParseNode.leaf(actual));
        }
    }

    private void expand(NonTerminal nonTerminal, Token lookahead,
                        List<ParseNode> siblings, Deque<Frame> stack) {
        Production production = table.lookup(nonTerminal, lookahead.type()).orElseThrow(
                () -> new ParseException(lookahead, "Expected one of " + table.expected(nonTerminal)));

        List<ParseNode> children = new ArrayList<>();
        siblings.add(ParseNode.branch(nonTerminal, children));

        List<Symbol> right = production.right();
        for (int i = right.size() - 1; i >= 0; i--) {
            stack.push(new Frame(right.get(i), children));
        }
    }

    /**
     * Запис стека: символ, який ще належить обробити, і список дітей, до якого він належить.
     */
    private record Frame(Symbol symbol, List<ParseNode> siblings) {
    }
}

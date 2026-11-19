package ast;

import grammar.NonTerminal;
import lexer.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Вузол дерева синтаксичного розбору.
 *
 * <p>Дерево будується класом {@link parser.Parser} для будь-якої
 * {@link grammar.Grammar}, тому має лише два види вузлів і не залежить
 * від мови:
 * <ul>
 *   <li>{@link Branch} - нетермінал зі своїм числовим кодом і дітьми
 *       у порядку правої частини використаної продукції;</li>
 *   <li>{@link Leaf} - термінал, що несе токен із лексера.</li>
 * </ul>
 * Кожен термінал входу, включно з роздільниками й дужками, присутній
 * як {@link Leaf}. Токен кінця файлу - ні.
 */
public sealed interface ParseNode {

    /**
     * Повертає всі токени під цим вузлом у порядку джерела.
     */
    List<Token> tokens();

    /**
     * Створює вузол-гілку. Список використовується як є, див. {@link Branch#children()}.
     */
    static Branch branch(NonTerminal symbol, List<ParseNode> children) {
        return new Branch(symbol, children);
    }

    /**
     * Створює вузол-листок.
     */
    static Leaf leaf(Token token) {
        return new Leaf(token);
    }

    /**
     * Вузол-нетермінал.
     *
     * @param symbol   нетермінал
     * @param children діти в порядку джерела; це лише для читання представлення списку,
     *                 переданого в конструктор, тож парсер може заповнювати список після
     *                 створення вузла
     */
    record Branch(NonTerminal symbol, List<ParseNode> children) implements ParseNode {

        public Branch {
            Objects.requireNonNull(symbol, "symbol");
            children = Collections.unmodifiableList(children);
        }

        /**
         * Повертає числовий код нетермінала.
         */
        public int code() {
            return symbol.code();
        }

        /**
         * Повертає {@code true}, якщо цей вузол є заданим нетерміналом.
         */
        public boolean is(NonTerminal expected) {
            return symbol.equals(expected);
        }

        /**
         * Повертає першого прямого нащадка, що є заданим нетерміналом.
         */
        public Optional<Branch> findChild(NonTerminal expected) {
            for (ParseNode child : children) {
                if (child instanceof Branch branch && branch.is(expected)) {
                    return Optional.of(branch);
                }
            }
            return Optional.empty();
        }

        /**
         * Повертає першого прямого нащадка, що є заданим нетерміналом.
         */
        public Branch child(NonTerminal expected) {
            return findChild(expected).orElseThrow(
                    () -> new IllegalStateException(symbol + " has no child " + expected));
        }

        /**
         * Повертає єдиного нащадка, який має бути нетерміналом.
         *
         * @throws IllegalStateException якщо вузол має іншу форму
         */
        public Branch onlyChildBranch() {
            if (children.size() == 1 && children.getFirst() instanceof Branch branch) {
                return branch;
            }
            throw new IllegalStateException(symbol + " must have exactly one non-terminal child");
        }

        @Override
        public List<Token> tokens() {
            List<Token> result = new ArrayList<>();
            for (ParseNode child : children) {
                result.addAll(child.tokens());
            }
            return result;
        }
    }

    /**
     * Вузол-термінал.
     *
     * @param token токен із лексера
     */
    record Leaf(Token token) implements ParseNode {

        public Leaf {
            Objects.requireNonNull(token, "token");
        }

        @Override
        public List<Token> tokens() {
            return List.of(token);
        }
    }
}

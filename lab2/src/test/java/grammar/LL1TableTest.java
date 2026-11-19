package grammar;

import lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;

import static grammar.GeometryGrammar.BUILD;
import static grammar.GeometryGrammar.LINE_THROUGH;
import static grammar.GeometryGrammar.POINT_DECL;
import static grammar.GeometryGrammar.SENTENCE;
import static grammar.GeometryGrammar.SENTENCE_LIST;
import static grammar.GeometryGrammar.SENTENCE_LIST_TAIL;
import static org.junit.jupiter.api.Assertions.*;

class LL1TableTest {
    private static final Grammar GEOMETRY = GeometryGrammar.grammar();
    private static final LL1Table TABLE = new LL1Table(GEOMETRY, new FirstFollowSets(GEOMETRY));

    private static Terminal t(TokenType type) {
        return new Terminal(type);
    }

    private static LL1Table tableOf(Grammar grammar) {
        return new LL1Table(grammar, new FirstFollowSets(grammar));
    }

    @Test
    void geometryGrammarIsLl1() {
        assertDoesNotThrow(() -> tableOf(GEOMETRY));
    }

    @Test
    void sentenceIsChosenByItsFirstToken() {
        assertEquals(POINT_DECL, TABLE.lookup(SENTENCE, TokenType.POINT).orElseThrow().right().getFirst());
        assertEquals(LINE_THROUGH, TABLE.lookup(SENTENCE, TokenType.DRAW).orElseThrow().right().getFirst());
        assertEquals(BUILD, TABLE.lookup(SENTENCE, TokenType.BUILD).orElseThrow().right().getFirst());
    }

    @Test
    void tailChoosesSeparatorOrEpsilon() {
        Production onSemicolon = TABLE.lookup(SENTENCE_LIST_TAIL, TokenType.SEMICOLON).orElseThrow();
        Production onEnd = TABLE.lookup(SENTENCE_LIST_TAIL, TokenType.END).orElseThrow();

        assertEquals(SENTENCE_LIST, onSemicolon.right().get(1));
        assertTrue(onEnd.isEpsilon());
    }

    @Test
    void lookupOfImpossibleTokenIsEmpty() {
        assertEquals(Optional.empty(), TABLE.lookup(SENTENCE, TokenType.COMMA));
        assertEquals(Optional.empty(), TABLE.lookup(new NonTerminal(99, "Unknown"), TokenType.COMMA));
    }

    @Test
    void expectedTokensOfSentenceAreItsFirstSet() {
        assertEquals(EnumSet.of(TokenType.POINT, TokenType.DRAW, TokenType.BUILD), TABLE.expected(SENTENCE));
    }

    @Test
    void rejectsGrammarWithCommonPrefix() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal item = new NonTerminal(1, "Item");
        Grammar grammar = Grammar.builder(start)
                .rule(start, item, t(TokenType.END))
                .rule(item, t(TokenType.IDENTIFIER))
                .rule(item, t(TokenType.IDENTIFIER), t(TokenType.COMMA), t(TokenType.IDENTIFIER))
                .build();

        GrammarException error = assertThrows(GrammarException.class, () -> tableOf(grammar));
        assertTrue(error.getMessage().contains("not LL(1)"));
    }

    @Test
    void rejectsLeftRecursiveGrammar() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal list = new NonTerminal(1, "List");
        Grammar grammar = Grammar.builder(start)
                .rule(start, list, t(TokenType.END))
                .rule(list, list, t(TokenType.COMMA), t(TokenType.IDENTIFIER))
                .rule(list, t(TokenType.IDENTIFIER))
                .build();

        assertThrows(GrammarException.class, () -> tableOf(grammar));
    }

    @Test
    void rejectsConflictBetweenFirstAndFollowOfNullableRule() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal tail = new NonTerminal(1, "Tail");
        Grammar grammar = Grammar.builder(start)
                .rule(start, tail, t(TokenType.COMMA))
                .rule(tail, t(TokenType.COMMA))
                .rule(tail)
                .build();

        assertThrows(GrammarException.class, () -> tableOf(grammar));
    }
}

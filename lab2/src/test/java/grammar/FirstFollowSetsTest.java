package grammar;

import lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static grammar.GeometryGrammar.BUILD_KIND;
import static grammar.GeometryGrammar.NAME;
import static grammar.GeometryGrammar.NAME_LIST;
import static grammar.GeometryGrammar.NAME_LIST_TAIL;
import static grammar.GeometryGrammar.PROGRAM;
import static grammar.GeometryGrammar.SENTENCE;
import static grammar.GeometryGrammar.SENTENCE_LIST_TAIL;
import static org.junit.jupiter.api.Assertions.*;

class FirstFollowSetsTest {
    private static final FirstFollowSets GEOMETRY = new FirstFollowSets(GeometryGrammar.grammar());

    private static Terminal t(TokenType type) {
        return new Terminal(type);
    }

    @Test
    void firstOfSentenceIsTheThreeSentenceStarts() {
        assertEquals(EnumSet.of(TokenType.POINT, TokenType.DRAW, TokenType.BUILD), GEOMETRY.first(SENTENCE));
    }

    @Test
    void firstOfBuildKindIsTheThreeKindWords() {
        assertEquals(EnumSet.of(TokenType.PERPENDICULAR, TokenType.TRIANGLE, TokenType.SEGMENT),
                GEOMETRY.first(BUILD_KIND));
    }

    @Test
    void listTailsAreNullableAndOthersAreNot() {
        assertTrue(GEOMETRY.isNullable(SENTENCE_LIST_TAIL));
        assertTrue(GEOMETRY.isNullable(NAME_LIST_TAIL));
        assertFalse(GEOMETRY.isNullable(SENTENCE));
        assertFalse(GEOMETRY.isNullable(NAME_LIST));
    }

    @Test
    void followOfStartSymbolIsEof() {
        assertEquals(EnumSet.of(TokenType.EOF), GEOMETRY.follow(PROGRAM));
    }

    @Test
    void followOfSentenceListTailIsEndMarker() {
        assertEquals(EnumSet.of(TokenType.END), GEOMETRY.follow(SENTENCE_LIST_TAIL));
    }

    @Test
    void followOfNameListTailIsSeparatorOrEndMarker() {
        assertEquals(EnumSet.of(TokenType.SEMICOLON, TokenType.END), GEOMETRY.follow(NAME_LIST_TAIL));
    }

    @Test
    void followOfNameCollectsEverythingThatCanComeAfterAName() {
        assertEquals(EnumSet.of(TokenType.LPAREN, TokenType.COMMA, TokenType.TO,
                        TokenType.RPAREN, TokenType.SEMICOLON, TokenType.END),
                GEOMETRY.follow(NAME));
    }

    @Test
    void firstOfSequenceSkipsNullableSymbols() {
        NonTerminal start = new NonTerminal(0, "Start");
        NonTerminal a = new NonTerminal(1, "A");
        NonTerminal b = new NonTerminal(2, "B");
        Grammar grammar = Grammar.builder(start)
                .rule(start, a, b, t(TokenType.END))
                .rule(a, t(TokenType.IDENTIFIER))
                .rule(a)
                .rule(b, t(TokenType.COMMA))
                .rule(b)
                .build();
        FirstFollowSets sets = new FirstFollowSets(grammar);

        assertEquals(EnumSet.of(TokenType.IDENTIFIER, TokenType.COMMA, TokenType.END),
                sets.first(start));
        assertEquals(EnumSet.of(TokenType.COMMA, TokenType.END), sets.follow(a));
        assertEquals(EnumSet.of(TokenType.END), sets.follow(b));
        assertEquals(EnumSet.of(TokenType.IDENTIFIER, TokenType.COMMA),
                sets.firstOf(List.of(a, b)));
        assertTrue(sets.isNullable(List.of(a, b)));
        assertFalse(sets.isNullable(List.of(a, t(TokenType.END))));
        assertTrue(sets.firstOf(List.of()).isEmpty());
    }

    @Test
    void firstOfUnknownNonTerminalIsRejected() {
        assertThrows(NullPointerException.class, () -> GEOMETRY.first(new NonTerminal(99, "Unknown")));
    }
}

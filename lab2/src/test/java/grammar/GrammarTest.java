package grammar;

import lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrammarTest {
    private static final NonTerminal START = new NonTerminal(0, "Start");
    private static final NonTerminal ITEM = new NonTerminal(1, "Item");

    private static Terminal t(TokenType type) {
        return new Terminal(type);
    }

    @Test
    void groupsProductionsByLeftSideInDeclarationOrder() {
        Grammar grammar = Grammar.builder(START)
                .rule(START, ITEM, t(TokenType.END))
                .rule(ITEM, t(TokenType.IDENTIFIER))
                .rule(ITEM, t(TokenType.NUMBER))
                .build();

        assertEquals(START, grammar.start());
        assertEquals(3, grammar.productions().size());
        assertEquals(2, grammar.productionsFor(ITEM).size());
        assertEquals(List.of(START, ITEM), List.copyOf(grammar.nonTerminals()));
    }

    @Test
    void ruleWithoutSymbolsIsEpsilonProduction() {
        Grammar grammar = Grammar.builder(START)
                .rule(START, ITEM, t(TokenType.END))
                .rule(ITEM)
                .build();

        assertTrue(grammar.productionsFor(ITEM).getFirst().isEpsilon());
        assertFalse(grammar.productionsFor(START).getFirst().isEpsilon());
    }

    @Test
    void productionToStringShowsTheRule() {
        Production production = new Production(START, List.of(ITEM, t(TokenType.END)));
        assertEquals("<Start> ::= <Item> END", production.toString());
        assertEquals("<Item> ::= <empty>", new Production(ITEM, List.of()).toString());
    }

    @Test
    void productionsForUnknownNonTerminalIsEmpty() {
        Grammar grammar = Grammar.builder(START).rule(START, t(TokenType.END)).build();
        assertTrue(grammar.productionsFor(ITEM).isEmpty());
    }

    @Test
    void rejectsGrammarWithoutProductions() {
        assertThrows(GrammarException.class, () -> Grammar.builder(START).build());
    }

    @Test
    void rejectsStartSymbolWithoutProductions() {
        assertThrows(GrammarException.class,
                () -> Grammar.builder(START).rule(ITEM, t(TokenType.IDENTIFIER)).build());
    }

    @Test
    void rejectsUndefinedNonTerminalOnRightSide() {
        assertThrows(GrammarException.class,
                () -> Grammar.builder(START).rule(START, ITEM, t(TokenType.END)).build());
    }

    @Test
    void rejectsDuplicateNonTerminalCodes() {
        NonTerminal sameCode = new NonTerminal(1, "Other");
        assertThrows(GrammarException.class, () -> Grammar.builder(START)
                .rule(START, ITEM, sameCode, t(TokenType.END))
                .rule(ITEM)
                .rule(sameCode)
                .build());
    }

    @Test
    void rejectsDuplicateNonTerminalNames() {
        NonTerminal sameName = new NonTerminal(2, "Item");
        assertThrows(GrammarException.class, () -> Grammar.builder(START)
                .rule(START, ITEM, sameName, t(TokenType.END))
                .rule(ITEM)
                .rule(sameName)
                .build());
    }

    @Test
    void rejectsBlankNonTerminalName() {
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal(5, " "));
    }
}

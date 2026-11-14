package ast;

import lexer.Token;

import java.util.List;

/**
 * Дерево синтаксичного розбору. Кожен record відповідає одному
 * нетерміналу граматики мови:
 *
 * <текст>            ::= <список_речень> #
 * <список_речень>    ::= <речення> <продовження_списку>
 * <продовження_списку> ::= ; <список_речень> | <пусто>
 *
 * <речення>          ::= <точка> | <пряма_через> | <перпендикуляр>
 *                       | <трикутник> | <відрізок>
 *
 * <точка>            ::= ТОЧКА <ім'я> ( <число> , <число> )
 * <пряма_через>      ::= ПРОВЕСТИ ПРЯМУ ЧЕРЕЗ <пара_імен>
 * <перпендикуляр>    ::= ПОБУДУВАТИ ПЕРПЕНДИКУЛЯР ДО ПРЯМОЇ ( <пара_імен> ) В ТОЧЦІ <ім'я>
 * <трикутник>        ::= ПОБУДУВАТИ ТРИКУТНИК ПО ТРЬОМ ВЕРШИНАМ <список_імен>
 * <відрізок>         ::= ПОБУДУВАТИ ВІДРІЗОК ВІД <ім'я> ДО <ім'я>
 *
 * <список_імен>      ::= <ім'я> <продовження_імен>
 * <продовження_імен> ::= , <список_імен> | <пусто>
 * <пара_імен>        ::= <ім'я> , <ім'я>
 * <ім'я>             ::= IDENTIFIER
 * <число>            ::= NUMBER
 *
 * Кожен нетермінал має власний числовий код (поле CODE), яким
 * словосполучення граматики замінюються на стандартні коди.
 */
public sealed interface ParseNode {

    int code();

    record Program(List<Sentence> sentences) implements ParseNode {
        public static final int CODE = 0;
        public int code() { return CODE; }
    }

    record Sentence(ParseNode body) implements ParseNode {
        public static final int CODE = 1;
        public int code() { return CODE; }
    }

    record PointDecl(Name name, NumberLit x, NumberLit y) implements ParseNode {
        public static final int CODE = 2;
        public int code() { return CODE; }
    }

    record LineThrough(NamePair points) implements ParseNode {
        public static final int CODE = 3;
        public int code() { return CODE; }
    }

    record Perpendicular(NamePair base, Name target) implements ParseNode {
        public static final int CODE = 4;
        public int code() { return CODE; }
    }

    record Triangle(NameList vertices) implements ParseNode {
        public static final int CODE = 5;
        public int code() { return CODE; }
    }

    record Segment(Name from, Name to) implements ParseNode {
        public static final int CODE = 6;
        public int code() { return CODE; }
    }

    record NameList(List<Name> names) implements ParseNode {
        public static final int CODE = 7;
        public int code() { return CODE; }
    }

    record NamePair(Name a, Name b) implements ParseNode {
        public static final int CODE = 8;
        public int code() { return CODE; }
    }

    record Name(Token token) implements ParseNode {
        public static final int CODE = 9;
        public int code() { return CODE; }
    }

    record NumberLit(Token token) implements ParseNode {
        public static final int CODE = 10;
        public int code() { return CODE; }
    }
}
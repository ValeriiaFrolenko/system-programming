package vfrolenko;

public enum TokenType {
    // Literals
    INTEGER,
    FLOAT,
    STRING,

    // Identifier
    IDENTIFIER,

    // Keywords
    KEYWORD,

    // Built-in functions
    BUILTIN_FUNCTION,

    // Arithmetic operators
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,

    // Comparison operators
    EQUAL,
    NOT_EQUAL,
    LESS,
    GREATER,
    LESS_EQUAL,
    GREATER_EQUAL,

    // Assignment
    ASSIGN,

    // Delimiters
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    SEMICOLON,
    COMMA,

    // Special
    COMMENT,
    EOF,

    // Error
    ERROR
}
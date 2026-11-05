package lexer;

public enum TokenType {
    // Ключові слова (основи слів)
    POINT,          // ТОЧК(А)      - оголошення точки
    POINT_LOC,      // ТОЧЦ(І)      - у локативі ("в точці")
    LINE,           // ПРЯМ(А/У/ОЇ) - пряма
    THROUGH,        // ЧЕРЕЗ
    DRAW,           // ПРОВЕСТ(И)
    BUILD,          // ПОБУДУВА(ТИ)
    PERPENDICULAR,  // ПЕРПЕНДИКУЛЯР
    TO,             // ДО
    AT,             // В
    TRIANGLE,       // ТРИКУТНИК
    BY,             // ПО
    THREE,          // ТРЬОМ
    VERTEX,         // ВЕРШИН(А/И/АМ)

    // Літерали
    IDENTIFIER,
    NUMBER,

    // Пунктуація
    COMMA,          // ,
    LPAREN,         // (
    RPAREN,         // )

    // Службові
    EOF
}
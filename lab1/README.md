# Лабораторна робота №1 — Лексичний аналізатор

**Студентка:** Фроленко Валерія  

---

## Опис

Лексичний аналізатор для фрагменту мови програмування. При кожному зверненні до методу `nextToken()` повертає наступну лексему з вхідного тексту із зазначенням її типу та позиції (рядок, колонка).

---

## Класи лексем

| Тип | Опис | Приклади |
|-----|------|---------|
| `IDENTIFIER` | Ідентифікатор — літери латиниці та літери з прізвища **Фроленко** (`Ф ф Р р О о Л л Е е Н н К к`), цифри (не на початку) | `result`, `Фроленко`, `x1` |
| `KEYWORD` | Зарезервоване слово | `if`, `else`, `while`, `for`, `return`, `int`, `float`, `string`, `void`, `do`, `break`, `continue`, `true`, `false` |
| `BUILTIN_FUNCTION` | Вбудована функція | `sin`, `cos`, `tan`, `sqrt`, `abs`, `log`, `exp`, `pow`, `floor`, `ceil`, `round`, `min`, `max` |
| `INTEGER` | Ціле число | `42`, `0`, `100` |
| `FLOAT` | Дробове число | `3.14`, `0.0` |
| `STRING` | Рядковий літерал | `"Валерія Фроленко"` |
| `PLUS` `MINUS` `MULTIPLY` `DIVIDE` | Арифметичні оператори | `+`, `-`, `*`, `/` |
| `EQUAL` `NOT_EQUAL` `LESS` `GREATER` `LESS_EQUAL` `GREATER_EQUAL` | Оператори порівняння | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| `ASSIGN` | Присвоєння | `=` |
| `LPAREN` `RPAREN` `LBRACE` `RBRACE` `SEMICOLON` `COMMA` | Розділювачі | `(`, `)`, `{`, `}`, `;`, `,` |
| `COMMENT` | Однорядковий коментар | `// текст` |
| `ERROR` | Помилкова лексема | `3.14.5`, `123abc`, `@` |
| `EOF` | Кінець вхідного тексту | |

---

## Структура проєкту

```
lab1/
├── src/
│   └── main/
│       ├── java/vfrolenko/
│       │   ├── TokenType.java   # enum типів токенів
│       │   ├── Token.java       # record токена (тип, значення, позиція)
│       │   ├── Lexer.java       # FSM-лексер, метод nextToken()
│       │   └── Main.java        # точка входу, читання з файлу або stdin
│       └── resources/
│           └── program.txt      # приклад вхідної програми
└── build.gradle.kts
```

---

## Запуск

### IntelliJ IDEA

**Run → Edit Configurations → Program arguments:**
```
src/main/resources/program.txt
```

### Термінал

```bash
./gradlew run --args="src/main/resources/program.txt"
```

або зі stdin:

```bash
./gradlew run < src/main/resources/program.txt
```

---

## Приклад роботи

**Вхід (`program.txt`):**
```
int Фроленко = 42;
float result = 3.14 * Фроленко;
if (result > 100) {
    result = sqrt(result) + cos(0.0);
}
float bad1 = 3.14.5;
int bad2 = 123abc;
```

**Вихід:**
```
=== TOKENS ===
Token{type=KEYWORD              value='int'            line=1, col=1}
Token{type=IDENTIFIER           value='Фроленко'       line=1, col=5}
Token{type=ASSIGN               value='='              line=1, col=14}
Token{type=INTEGER              value='42'             line=1, col=16}
Token{type=SEMICOLON            value=';'              line=1, col=18}
...
Token{type=ERROR                value='Invalid number literal: 3.14.5'  line=6, col=14}
Token{type=ERROR                value='Invalid number literal: 123abc'  line=7, col=12}

=== SUMMARY ===
Total tokens : 35
Error tokens : 2
```

# Лабораторна робота №1 — Лексичний аналізатор

**Студентки:** Фроленко Валерія  

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

**Вхід** (`src/main/resources/program.txt`, скорочений приклад):

```c
int Фроленко = 42;
float x = 3.14 * Фроленко;
float bad1 = 3.14.5;
int bad2 = 123abc;
```

**Команда:**

```bash
./gradlew run --args="src/main/resources/program.txt"
```

**Вихід:**

```
=== FILE: src/main/resources/program.txt ===

=== TOKENS ===
Token{type=KEYWORD              value='int'           line=1, col=1}
Token{type=IDENTIFIER           value='Фроленко'      line=1, col=5}
Token{type=ASSIGN               value='='             line=1, col=14}
Token{type=INTEGER              value='42'            line=1, col=16}
Token{type=SEMICOLON            value=';'             line=1, col=18}
Token{type=KEYWORD              value='float'         line=2, col=1}
Token{type=IDENTIFIER           value='x'             line=2, col=7}
Token{type=ASSIGN               value='='             line=2, col=9}
Token{type=FLOAT                value='3.14'          line=2, col=11}
Token{type=MULTIPLY             value='*'             line=2, col=16}
Token{type=IDENTIFIER           value='Фроленко'      line=2, col=18}
Token{type=SEMICOLON            value=';'             line=2, col=26}
Token{type=KEYWORD              value='float'         line=3, col=1}
Token{type=IDENTIFIER           value='bad1'          line=3, col=7}
Token{type=ASSIGN               value='='             line=3, col=12}
Token{type=ERROR                value='Invalid number literal: 3.14.5' line=3, col=14}
Token{type=SEMICOLON            value=';'             line=3, col=20}
Token{type=KEYWORD              value='int'           line=4, col=1}
Token{type=IDENTIFIER           value='bad2'          line=4, col=5}
Token{type=ASSIGN               value='='             line=4, col=10}
Token{type=ERROR                value='Invalid number literal: 123abc' line=4, col=12}
Token{type=SEMICOLON            value=';'             line=4, col=18}
Token{type=EOF                  value=''              line=4, col=19}

=== SUMMARY ===
Total tokens : 22
Error tokens : 2
```

Повний `program.txt` (23 рядки) дає 101 токен і 3 помилки — див. скріншот запуску.
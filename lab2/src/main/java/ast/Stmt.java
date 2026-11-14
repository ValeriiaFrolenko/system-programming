package ast;

import java.util.List;

/**
 * Спрощене абстрактне синтаксичне дерево (AST) для семантичного аналізу.
 *
 * <p>Отримується з {@link ParseNode} класом {@link parser.TreeFolder}.
 * На відміну від {@code ParseNode}, тут немає службових вузлів
 * ({@code Sentence}, {@code NamePair}, {@code NumberLit} тощо): усе
 * зведено до найпростіших даних, з якими зручно працювати семантиці —
 * рядки для імен, {@code double} для координат.
 *
 * <p>Саме {@code Stmt.Program} приймає {@link sema.SemanticAnalyzer}.
 */
public sealed interface Stmt {

    /** Корінь AST — список речень верхнього рівня. */
    record Program(List<Stmt> statements) implements Stmt {}

    /** Оголошення точки з координатами. */
    record PointDecl(String name, double x, double y) implements Stmt {}

    /** Пряма через дві точки. */
    record LineThrough(String a, String b) implements Stmt {}

    /** Перпендикуляр до прямої {@code (baseA, baseB)} через точку {@code targetPoint}. */
    record Perpendicular(String baseA, String baseB, String targetPoint) implements Stmt {}

    /** Трикутник за трьома вершинами. */
    record Triangle(String a, String b, String c) implements Stmt {}

    /** Відрізок між двома точками. */
    record Segment(String a, String b) implements Stmt {}
}

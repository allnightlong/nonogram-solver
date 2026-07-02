package ru.megadevelopers.nanogram.solver;

import ru.megadevelopers.nanogram.model.Cell;

public sealed interface SolveResult {
    record Solved(Cell[][] board) implements SolveResult {}

    record Ambiguous(Cell[][] partialBoard) implements SolveResult {}

    record NoSolution() implements SolveResult {}
}

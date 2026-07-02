package ru.megadevelopers.nanogram.solver.v4;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DpSolverTest {

    private final DpSolver solver = new DpSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(List.of(3), List.of(3), List.of(3)),
                List.of(List.of(3), List.of(3), List.of(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatNeedsCrossLinePropagationButNoGuessing() {
        Puzzle puzzle = new Puzzle(
                List.of(List.of(1), List.of(3), List.of(1)),
                List.of(List.of(1), List.of(3), List.of(1)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatRequiresGuessing() {
        // Same permutation-matrix puzzle that stumps pure propagation in
        // v1/v3: every row/column clue is [1] on a 3x3 board, so every
        // block's leftmost and rightmost feasible window spans the whole
        // line with no forced overlap - DP propagation alone makes zero
        // progress, same reasoning as the candidate-based solvers, just via
        // window overlap instead of candidate-set intersection.
        Puzzle puzzle = new Puzzle(
                List.of(List.of(1), List.of(1), List.of(1)),
                List.of(List.of(1), List.of(1), List.of(1)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void reportsNoSolutionWhenClueExceedsLineLength() {
        Puzzle puzzle = new Puzzle(List.of(List.of(2)), List.of(List.of(2)), 1, 1);

        assertInstanceOf(SolveResult.NoSolution.class, solver.solve(puzzle));
    }
}

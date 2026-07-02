package ru.megadevelopers.nanogram.solver;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HybridSolverTest {

    private final HybridSolver solver = new HybridSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(List.of(3), List.of(3), List.of(3)),
                List.of(List.of(3), List.of(3), List.of(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatRequiresGuessing() {
        // Same permutation-matrix puzzle PropagationSolver reports as
        // Ambiguous on - HybridSolver must fall back to guessing and reach
        // a genuine solution.
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

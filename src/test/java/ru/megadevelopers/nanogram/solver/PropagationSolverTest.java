package ru.megadevelopers.nanogram.solver;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PropagationSolverTest {

    private final PropagationSolver solver = new PropagationSolver();

    @Test
    void solvesPuzzleFullyDeterminedByPropagation() {
        Puzzle puzzle = new Puzzle(
                List.of(List.of(1), List.of(3), List.of(1)),
                List.of(List.of(1), List.of(3), List.of(1)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void reportsAmbiguousWhenPropagationCannotDetermineEveryCell() {
        // Every row/column clue is [1] on a 3x3 board: any permutation matrix
        // satisfies every line's clue, so no line's candidates ever share a
        // common forced bit and propagation makes zero progress.
        Puzzle puzzle = new Puzzle(
                List.of(List.of(1), List.of(1), List.of(1)),
                List.of(List.of(1), List.of(1), List.of(1)),
                3, 3);

        assertInstanceOf(SolveResult.Ambiguous.class, solver.solve(puzzle));
    }

    @Test
    void reportsNoSolutionWhenClueExceedsLineLength() {
        Puzzle puzzle = new Puzzle(List.of(List.of(2)), List.of(List.of(2)), 1, 1);

        assertInstanceOf(SolveResult.NoSolution.class, solver.solve(puzzle));
    }
}

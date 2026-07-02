package ru.megadevelopers.nanogram.solver.v3;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HybridSolverTest {

    private final HybridSolver solver = new HybridSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(Clue.of(3), Clue.of(3), Clue.of(3)),
                List.of(Clue.of(3), Clue.of(3), Clue.of(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatRequiresGuessing() {
        // Same permutation-matrix puzzle PropagationSolver reports as
        // Ambiguous on - HybridSolver must fall back to guessing and reach
        // a genuine solution.
        Puzzle puzzle = new Puzzle(
                List.of(Clue.of(1), Clue.of(1), Clue.of(1)),
                List.of(Clue.of(1), Clue.of(1), Clue.of(1)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void reportsNoSolutionWhenClueExceedsLineLength() {
        Puzzle puzzle = new Puzzle(List.of(Clue.of(2)), List.of(Clue.of(2)), 1, 1);

        assertInstanceOf(SolveResult.NoSolution.class, solver.solve(puzzle));
    }
}

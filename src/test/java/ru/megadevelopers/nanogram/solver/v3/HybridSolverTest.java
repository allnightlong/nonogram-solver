package ru.megadevelopers.nanogram.solver.v3;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static ru.megadevelopers.nanogram.model.Clue.clueOf;

class HybridSolverTest {

    private final HybridSolver solver = new HybridSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(clueOf(3), clueOf(3), clueOf(3)),
                List.of(clueOf(3), clueOf(3), clueOf(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatRequiresGuessing() {
        // Same permutation-matrix puzzle PropagationSolver reports as
        // Ambiguous on - HybridSolver must fall back to guessing and reach
        // a genuine solution.
        Puzzle puzzle = new Puzzle(
                List.of(clueOf(1), clueOf(1), clueOf(1)),
                List.of(clueOf(1), clueOf(1), clueOf(1)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void reportsNoSolutionWhenClueExceedsLineLength() {
        Puzzle puzzle = new Puzzle(List.of(clueOf(2)), List.of(clueOf(2)), 1, 1);

        assertInstanceOf(SolveResult.NoSolution.class, solver.solve(puzzle));
    }
}

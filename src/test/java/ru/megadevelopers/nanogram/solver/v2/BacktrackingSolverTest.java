package ru.megadevelopers.nanogram.solver.v2;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static ru.megadevelopers.nanogram.model.Clue.clueOf;

class BacktrackingSolverTest {

    private final BacktrackingSolver solver = new BacktrackingSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(clueOf(3), clueOf(3), clueOf(3)),
                List.of(clueOf(3), clueOf(3), clueOf(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatPropagationAloneCannotFullyDetermine() {
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

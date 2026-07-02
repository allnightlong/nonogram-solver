package ru.megadevelopers.nanogram.solver.v2;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BacktrackingSolverTest {

    private final BacktrackingSolver solver = new BacktrackingSolver();

    @Test
    void solvesSimplePuzzle() {
        Puzzle puzzle = new Puzzle(
                List.of(List.of(3), List.of(3), List.of(3)),
                List.of(List.of(3), List.of(3), List.of(3)),
                3, 3);

        assertInstanceOf(SolveResult.Solved.class, solver.solve(puzzle));
    }

    @Test
    void solvesPuzzleThatPropagationAloneCannotFullyDetermine() {
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

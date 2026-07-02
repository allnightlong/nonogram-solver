package ru.megadevelopers.nanogram.solver;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.solver.v1.PropagationSolver;
import ru.megadevelopers.nanogram.solver.v2.BacktrackingSolver;
import ru.megadevelopers.nanogram.solver.v3.HybridSolver;
import ru.megadevelopers.nanogram.solver.v4.DynamicProgrammingSolver;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SolverAgreementTest {

    private static Stream<Puzzle> uniquelySolvablePuzzles() {
        return Stream.of(
                new Puzzle(
                        List.of(Clue.of(3), Clue.of(3), Clue.of(3)),
                        List.of(Clue.of(3), Clue.of(3), Clue.of(3)),
                        3, 3),
                new Puzzle(
                        List.of(Clue.of(1), Clue.of(3), Clue.of(1)),
                        List.of(Clue.of(1), Clue.of(3), Clue.of(1)),
                        3, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("uniquelySolvablePuzzles")
    void allSolversAgreeOnUniquelySolvablePuzzles(Puzzle puzzle) {
        SolveResult propagationResult = new PropagationSolver().solve(puzzle);
        SolveResult backtrackingResult = new BacktrackingSolver().solve(puzzle);
        SolveResult hybridResult = new HybridSolver().solve(puzzle);
        SolveResult dpResult = new DynamicProgrammingSolver().solve(puzzle);

        assertInstanceOf(SolveResult.Solved.class, propagationResult);
        assertInstanceOf(SolveResult.Solved.class, backtrackingResult);
        assertInstanceOf(SolveResult.Solved.class, hybridResult);
        assertInstanceOf(SolveResult.Solved.class, dpResult);

        Cell[][] expected = ((SolveResult.Solved) propagationResult).board();
        assertEquals(Arrays.deepToString(expected),
                Arrays.deepToString(((SolveResult.Solved) backtrackingResult).board()));
        assertEquals(Arrays.deepToString(expected),
                Arrays.deepToString(((SolveResult.Solved) hybridResult).board()));
        assertEquals(Arrays.deepToString(expected),
                Arrays.deepToString(((SolveResult.Solved) dpResult).board()));
    }
}

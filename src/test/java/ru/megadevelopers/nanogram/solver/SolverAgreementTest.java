package ru.megadevelopers.nanogram.solver;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.megadevelopers.nanogram.model.Cell;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SolverAgreementTest {

    private static Stream<Puzzle> uniquelySolvablePuzzles() {
        return Stream.of(
                new Puzzle(
                        List.of(List.of(3), List.of(3), List.of(3)),
                        List.of(List.of(3), List.of(3), List.of(3)),
                        3, 3),
                new Puzzle(
                        List.of(List.of(1), List.of(3), List.of(1)),
                        List.of(List.of(1), List.of(3), List.of(1)),
                        3, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("uniquelySolvablePuzzles")
    void allSolversAgreeOnUniquelySolvablePuzzles(Puzzle puzzle) {
        SolveResult propagationResult = new PropagationSolver().solve(puzzle);
        SolveResult backtrackingResult = new BacktrackingSolver().solve(puzzle);
        SolveResult hybridResult = new HybridSolver().solve(puzzle);

        assertInstanceOf(SolveResult.Solved.class, propagationResult);
        assertInstanceOf(SolveResult.Solved.class, backtrackingResult);
        assertInstanceOf(SolveResult.Solved.class, hybridResult);

        Cell[][] expected = ((SolveResult.Solved) propagationResult).board();
        assertEquals(Arrays.deepToString(expected),
                Arrays.deepToString(((SolveResult.Solved) backtrackingResult).board()));
        assertEquals(Arrays.deepToString(expected),
                Arrays.deepToString(((SolveResult.Solved) hybridResult).board()));
    }
}

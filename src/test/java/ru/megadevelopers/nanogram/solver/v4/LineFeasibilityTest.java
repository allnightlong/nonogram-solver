package ru.megadevelopers.nanogram.solver.v4;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Cell;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineFeasibilityTest {

    @Test
    void wholeLineIsFeasibleWhenBlocksFitWithSlack() {
        Board board = new Board(1, 5);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(1, 2));

        assertTrue(feasibility.isFullyFeasible());
    }

    @Test
    void wholeLineIsInfeasibleWhenABlockCannotFit() {
        Board board = new Board(1, 1);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(2));

        assertFalse(feasibility.isFullyFeasible());
    }

    @Test
    void singleBlockWithNoSlackOnlyFitsAtOnePosition() {
        Board board = new Board(1, 3);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(3));

        assertTrue(feasibility.canPlaceBlockAt(0, 0, 3));
    }

    @Test
    void twoBlocksWithNoSlackEachHaveExactlyOneFeasibleStart() {
        // length 4, clue [1,2]: zero slack, unique solution [F,E,F,F]
        Board board = new Board(1, 4);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(1, 2));

        // block 0 (length 1): only candidateStart 0 works
        assertTrue(feasibility.canPlaceBlockAt(0, 0, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 1, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 2, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 3, 1));

        // block 1 (length 2): only candidateStart 2 works
        assertFalse(feasibility.canPlaceBlockAt(1, 0, 2));
        assertFalse(feasibility.canPlaceBlockAt(1, 1, 2));
        assertTrue(feasibility.canPlaceBlockAt(1, 2, 2));
    }

    @Test
    void twoBlocksWithOneSlackCellEachHaveTwoFeasibleStarts() {
        // length 5, clue [1,2]: one slack cell, three valid full arrangements
        // (only position 3 ends up forced - see LineForcedCellSolverTest)
        Board board = new Board(1, 5);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(1, 2));

        // block 0 (length 1): candidateStart 0 and 1 work, 2 and 3 don't
        assertTrue(feasibility.canPlaceBlockAt(0, 0, 1));
        assertTrue(feasibility.canPlaceBlockAt(0, 1, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 2, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 3, 1));

        // block 1 (length 2): candidateStart 2 and 3 work, 0 and 1 don't
        assertFalse(feasibility.canPlaceBlockAt(1, 0, 2));
        assertFalse(feasibility.canPlaceBlockAt(1, 1, 2));
        assertTrue(feasibility.canPlaceBlockAt(1, 2, 2));
        assertTrue(feasibility.canPlaceBlockAt(1, 3, 2));
    }

    @Test
    void aKnownFilledCellRestrictsPlacementToPositionsThatCoverIt() {
        Board board = new Board(1, 3);
        board.set(0, 0, Cell.FILLED);

        LineFeasibility feasibility = new LineFeasibility(board.row(0), List.of(1));

        assertTrue(feasibility.canPlaceBlockAt(0, 0, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 1, 1));
        assertFalse(feasibility.canPlaceBlockAt(0, 2, 1));
    }
}

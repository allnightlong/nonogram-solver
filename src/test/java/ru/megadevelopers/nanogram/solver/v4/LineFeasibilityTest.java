package ru.megadevelopers.nanogram.solver.v4;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Block;
import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineFeasibilityTest {

    @Test
    void wholeLineIsFeasibleWhenBlocksFitWithSlack() {
        Board board = new Board(1, 5);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), Clue.of(1, 2).blocks());

        assertTrue(feasibility.isFullyFeasible());
    }

    @Test
    void wholeLineIsInfeasibleWhenABlockCannotFit() {
        Board board = new Board(1, 1);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), Clue.of(2).blocks());

        assertFalse(feasibility.isFullyFeasible());
    }

    @Test
    void singleBlockWithNoSlackOnlyFitsAtOnePosition() {
        Board board = new Board(1, 3);
        Block block = Clue.of(3).blocks().get(0);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), Clue.of(3).blocks());

        assertTrue(feasibility.isFeasibleAt(block, 0));
    }

    @Test
    void twoBlocksWithNoSlackEachHaveExactlyOneFeasibleStart() {
        // length 4, clue [1,2]: zero slack, unique solution [F,E,F,F]
        Board board = new Board(1, 4);
        var blocks = Clue.of(1, 2).blocks();
        Block block0 = blocks.get(0);
        Block block1 = blocks.get(1);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), blocks);

        // block 0 (length 1): only start 0 works
        assertTrue(feasibility.isFeasibleAt(block0, 0));
        assertFalse(feasibility.isFeasibleAt(block0, 1));
        assertFalse(feasibility.isFeasibleAt(block0, 2));
        assertFalse(feasibility.isFeasibleAt(block0, 3));

        // block 1 (length 2): only start 2 works
        assertFalse(feasibility.isFeasibleAt(block1, 0));
        assertFalse(feasibility.isFeasibleAt(block1, 1));
        assertTrue(feasibility.isFeasibleAt(block1, 2));
    }

    @Test
    void twoBlocksWithOneSlackCellEachHaveTwoFeasibleStarts() {
        // length 5, clue [1,2]: one slack cell, three valid full arrangements
        // (only position 3 ends up forced - see LineForcedCellSolverTest)
        Board board = new Board(1, 5);
        var blocks = Clue.of(1, 2).blocks();
        Block block0 = blocks.get(0);
        Block block1 = blocks.get(1);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), blocks);

        // block 0 (length 1): starts 0 and 1 work, 2 and 3 don't
        assertTrue(feasibility.isFeasibleAt(block0, 0));
        assertTrue(feasibility.isFeasibleAt(block0, 1));
        assertFalse(feasibility.isFeasibleAt(block0, 2));
        assertFalse(feasibility.isFeasibleAt(block0, 3));

        // block 1 (length 2): starts 2 and 3 work, 0 and 1 don't
        assertFalse(feasibility.isFeasibleAt(block1, 0));
        assertFalse(feasibility.isFeasibleAt(block1, 1));
        assertTrue(feasibility.isFeasibleAt(block1, 2));
        assertTrue(feasibility.isFeasibleAt(block1, 3));
    }

    @Test
    void aKnownFilledCellRestrictsPlacementToPositionsThatCoverIt() {
        Board board = new Board(1, 3);
        board.set(0, 0, Cell.FILLED);
        Block block = Clue.of(1).blocks().get(0);

        LineFeasibility feasibility = LineFeasibility.analyze(board.row(0), Clue.of(1).blocks());

        assertTrue(feasibility.isFeasibleAt(block, 0));
        assertFalse(feasibility.isFeasibleAt(block, 1));
        assertFalse(feasibility.isFeasibleAt(block, 2));
    }
}

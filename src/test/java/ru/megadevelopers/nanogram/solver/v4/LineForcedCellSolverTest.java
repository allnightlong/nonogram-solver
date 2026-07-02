package ru.megadevelopers.nanogram.solver.v4;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Cell;
import static ru.megadevelopers.nanogram.model.Cell.*;
import ru.megadevelopers.nanogram.model.Clue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static ru.megadevelopers.nanogram.model.Clue.clueOf;

class LineForcedCellSolverTest {

    @Test
    void forcesEveryCellWhenABlockExactlyFillsTheLine() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(3));

        assertArrayEquals(new Cell[]{FILLED, FILLED, FILLED}, forced);
    }

    @Test
    void forcesNothingWhenABlockHasRoomToMoveAndNothingIsKnown() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(1));

        assertArrayEquals(new Cell[]{NO_VALUE, NO_VALUE, NO_VALUE}, forced);
    }

    @Test
    void aKnownFilledCellCanFullyResolveAnOtherwiseAmbiguousClue() {
        Board board = new Board(1, 3);
        board.set(0, 0, FILLED);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(1));

        assertArrayEquals(new Cell[]{FILLED, EMPTY, EMPTY}, forced);
    }

    @Test
    void solvesTwoBlocksWithNoSlackInOnePass() {
        Board board = new Board(1, 4);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(1, 2));

        assertArrayEquals(new Cell[]{FILLED, EMPTY, FILLED, FILLED}, forced);
    }


    @Test
    void partiallySolvesTwoBlocksWithSlack() {
        Board board = new Board(1, 5);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(1, 2));

        assertArrayEquals(new Cell[]{NO_VALUE, NO_VALUE, NO_VALUE, FILLED, NO_VALUE}, forced);
    }

    @Test
    void returnsNullWhenABlockCannotFitInTheLine() {
        Board board = new Board(1, 1);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf(2));

        assertNull(forced);
    }

    @Test
    void blankClueForcesTheWholeLineEmpty() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), clueOf());

        assertArrayEquals(new Cell[]{EMPTY, EMPTY, EMPTY}, forced);
    }
}

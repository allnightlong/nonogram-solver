package ru.megadevelopers.nanogram.solver.v4;

import org.junit.jupiter.api.Test;
import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LineForcedCellSolverTest {

    @Test
    void forcesEveryCellWhenABlockExactlyFillsTheLine() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(3));

        assertArrayEquals(new Cell[]{Cell.FILLED, Cell.FILLED, Cell.FILLED}, forced);
    }

    @Test
    void forcesNothingWhenABlockHasRoomToMoveAndNothingIsKnown() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(1));

        assertArrayEquals(new Cell[]{Cell.NO_VALUE, Cell.NO_VALUE, Cell.NO_VALUE}, forced);
    }

    @Test
    void aKnownFilledCellCanFullyResolveAnOtherwiseAmbiguousClue() {
        Board board = new Board(1, 3);
        board.set(0, 0, Cell.FILLED);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(1));

        assertArrayEquals(new Cell[]{Cell.FILLED, Cell.EMPTY, Cell.EMPTY}, forced);
    }

    @Test
    void solvesTwoBlocksWithNoSlackInOnePass() {
        Board board = new Board(1, 4);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(1, 2));

        assertArrayEquals(new Cell[]{Cell.FILLED, Cell.EMPTY, Cell.FILLED, Cell.FILLED}, forced);
    }


    @Test
    void partiallySolvesTwoBlocksWithSlack() {
        Board board = new Board(1, 5);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(1, 2));

        assertArrayEquals(new Cell[]{Cell.NO_VALUE, Cell.NO_VALUE, Cell.NO_VALUE, Cell.FILLED, Cell.NO_VALUE}, forced);
    }

    @Test
    void returnsNullWhenABlockCannotFitInTheLine() {
        Board board = new Board(1, 1);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of(2));

        assertNull(forced);
    }

    @Test
    void blankClueForcesTheWholeLineEmpty() {
        Board board = new Board(1, 3);

        Cell[] forced = LineForcedCellSolver.determineForced(board.row(0), Clue.of());

        assertArrayEquals(new Cell[]{Cell.EMPTY, Cell.EMPTY, Cell.EMPTY}, forced);
    }
}

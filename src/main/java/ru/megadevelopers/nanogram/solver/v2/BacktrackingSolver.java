package ru.megadevelopers.nanogram.solver.v2;

import ru.megadevelopers.nanogram.model.Cell;
import static ru.megadevelopers.nanogram.model.Cell.*;
import ru.megadevelopers.nanogram.model.Line;
import ru.megadevelopers.nanogram.solver.CellListener;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;
import ru.megadevelopers.nanogram.solver.Solver;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Naive row-major backtracking: try FILLED then EMPTY for each undetermined
 * cell, validating the affected row/column after every assignment. Complete,
 * but exponential - kept as a straightforward baseline to contrast with
 * HybridSolver's propagation-pruned search.
 */
public class BacktrackingSolver implements Solver {

    private static final int ANIMATION_DELAY_MILLIS = 100;

    @Override
    public SolveResult solve(Puzzle puzzle, CellListener listener) {
        Cell[][] board = new Cell[puzzle.height()][puzzle.width()];
        for (Cell[] row : board) Arrays.fill(row, NO_VALUE);

        boolean animate = listener != CellListener.NO_OP;
        boolean solved = solve(board, puzzle, listener, animate);

        return solved ? new SolveResult.Solved(board) : new SolveResult.NoSolution();
    }

    private boolean solve(Cell[][] board, Puzzle puzzle, CellListener listener, boolean animate) {
        if (animate) sleep();

        for (int row = 0; row < puzzle.height(); row++) {
            for (int column = 0; column < puzzle.width(); column++) {
                if (board[row][column] == NO_VALUE) {
                    if (trySet(board, puzzle, listener, animate, row, column, FILLED)) return true;
                    if (trySet(board, puzzle, listener, animate, row, column, EMPTY)) return true;

                    board[row][column] = NO_VALUE;
                    listener.onCellChanged(row, column, NO_VALUE);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean trySet(Cell[][] board, Puzzle puzzle, CellListener listener, boolean animate,
                            int row, int column, Cell value) {
        board[row][column] = value;
        listener.onCellChanged(row, column, value);
        return isValid(board, puzzle, row, column) && solve(board, puzzle, listener, animate);
    }

    private static void sleep() {
        try {
            Thread.sleep(ANIMATION_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isValid(Cell[][] board, Puzzle puzzle, int row, int column) {
        return isValidRow(board, puzzle, row) && isValidColumn(board, puzzle, column);
    }

    private static boolean isValidRow(Cell[][] board, Puzzle puzzle, int row) {
        List<BitSet> candidates = Line.candidates(puzzle.rowClues().get(row), puzzle.width());
        return Line.isValid(Arrays.asList(board[row]), candidates);
    }

    private static boolean isValidColumn(Cell[][] board, Puzzle puzzle, int column) {
        List<BitSet> candidates = Line.candidates(puzzle.columnClues().get(column), puzzle.height());
        List<Cell> currentLine = Arrays.stream(board).map(r -> r[column]).toList();
        return Line.isValid(currentLine, candidates);
    }
}

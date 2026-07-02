package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.solver.CellListener;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;
import ru.megadevelopers.nanogram.solver.Solver;

import java.util.Arrays;

/**
 * Steve Simpson's classic nonogram-solving algorithm: propagate row/column
 * constraints to a fixed point using a DP-based per-line forced-cell
 * computation (LineForcedCellSolver) - never enumerating candidate
 * placements - and when that alone can't fully determine the puzzle, guess
 * the most constrained undetermined cell and recurse, backtracking on
 * contradiction. Shares no code with HybridSolver (v3): same overall
 * shape, independently implemented, to isolate whether DP-based line
 * solving matters in practice versus candidate-list intersection.
 */
public class DynamicProgrammingSolver implements Solver {

    private record GuessCell(int row, int column) {}

    @Override
    public SolveResult solve(Puzzle puzzle, CellListener listener) {
        Cell[][] board = new Cell[puzzle.height()][puzzle.width()];
        for (Cell[] row : board) Arrays.fill(row, Cell.NO_VALUE);

        Cell[][] result = solve(board, puzzle);
        if (result == null) return new SolveResult.NoSolution();

        emit(result, listener);
        return new SolveResult.Solved(result);
    }

    private Cell[][] solve(Cell[][] board, Puzzle puzzle) {
        if (!propagateToFixedPoint(board, puzzle)) return null;
        if (isFullyDetermined(board)) return board;

        GuessCell guess = findMostConstrainedCell(board, puzzle);

        Cell[][] withFilled = copyOf(board);
        withFilled[guess.row()][guess.column()] = Cell.FILLED;
        Cell[][] result = solve(withFilled, puzzle);
        if (result != null) return result;

        Cell[][] withEmpty = copyOf(board);
        withEmpty[guess.row()][guess.column()] = Cell.EMPTY;
        return solve(withEmpty, puzzle);
    }

    private static boolean propagateToFixedPoint(Cell[][] board, Puzzle puzzle) {
        boolean changed;
        do {
            changed = false;
            for (int row = 0; row < puzzle.height(); row++) {
                Cell[] forced = LineForcedCellSolver.determineForced(board[row], puzzle.rowClues().get(row));
                if (forced == null) return false;
                if (apply(board[row], forced)) changed = true;
            }
            for (int column = 0; column < puzzle.width(); column++) {
                Cell[] currentColumn = extractColumn(board, column);
                Cell[] forced = LineForcedCellSolver.determineForced(currentColumn, puzzle.columnClues().get(column));
                if (forced == null) return false;
                if (applyColumn(board, column, forced)) changed = true;
            }
        } while (changed);
        return true;
    }

    private static boolean apply(Cell[] line, Cell[] forced) {
        boolean changed = false;
        for (int i = 0; i < line.length; i++) {
            if (forced[i] != Cell.NO_VALUE && line[i] == Cell.NO_VALUE) {
                line[i] = forced[i];
                changed = true;
            }
        }
        return changed;
    }

    private static Cell[] extractColumn(Cell[][] board, int column) {
        Cell[] result = new Cell[board.length];
        for (int row = 0; row < board.length; row++) result[row] = board[row][column];
        return result;
    }

    private static boolean applyColumn(Cell[][] board, int column, Cell[] forced) {
        boolean changed = false;
        for (int row = 0; row < board.length; row++) {
            if (forced[row] != Cell.NO_VALUE && board[row][column] == Cell.NO_VALUE) {
                board[row][column] = forced[row];
                changed = true;
            }
        }
        return changed;
    }

    private static boolean isFullyDetermined(Cell[][] board) {
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (cell == Cell.NO_VALUE) return false;
            }
        }
        return true;
    }

    /**
     * The undetermined cell belonging to whichever row or column has the
     * fewest remaining undetermined cells (ties broken by preferring rows,
     * then by increasing index) - the DP-world analogue of the
     * fewest-remaining-candidates heuristic HybridSolver uses.
     */
    private static GuessCell findMostConstrainedCell(Cell[][] board, Puzzle puzzle) {
        int bestRow = -1, bestRowCount = Integer.MAX_VALUE;
        for (int row = 0; row < puzzle.height(); row++) {
            int count = countUndetermined(board[row]);
            if (count > 0 && count < bestRowCount) {
                bestRow = row;
                bestRowCount = count;
            }
        }

        int bestColumn = -1, bestColumnCount = Integer.MAX_VALUE;
        for (int column = 0; column < puzzle.width(); column++) {
            int count = countUndeterminedColumn(board, column);
            if (count > 0 && count < bestColumnCount) {
                bestColumn = column;
                bestColumnCount = count;
            }
        }

        if (bestRow >= 0 && (bestColumn < 0 || bestRowCount <= bestColumnCount)) {
            return new GuessCell(bestRow, firstUndetermined(board[bestRow]));
        }
        return new GuessCell(firstUndeterminedColumn(board, bestColumn), bestColumn);
    }

    private static int countUndetermined(Cell[] line) {
        int count = 0;
        for (Cell cell : line) if (cell == Cell.NO_VALUE) count++;
        return count;
    }

    private static int countUndeterminedColumn(Cell[][] board, int column) {
        int count = 0;
        for (Cell[] row : board) if (row[column] == Cell.NO_VALUE) count++;
        return count;
    }

    private static int firstUndetermined(Cell[] line) {
        for (int i = 0; i < line.length; i++) {
            if (line[i] == Cell.NO_VALUE) return i;
        }
        throw new IllegalStateException("line has no undetermined cell");
    }

    private static int firstUndeterminedColumn(Cell[][] board, int column) {
        for (int row = 0; row < board.length; row++) {
            if (board[row][column] == Cell.NO_VALUE) return row;
        }
        throw new IllegalStateException("column has no undetermined cell");
    }

    private static Cell[][] copyOf(Cell[][] board) {
        Cell[][] copy = new Cell[board.length][];
        for (int i = 0; i < board.length; i++) copy[i] = board[i].clone();
        return copy;
    }

    private static void emit(Cell[][] board, CellListener listener) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                listener.onCellChanged(row, column, board[row][column]);
            }
        }
    }
}

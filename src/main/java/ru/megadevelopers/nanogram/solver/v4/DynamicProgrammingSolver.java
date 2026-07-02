package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.solver.CellListener;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;
import ru.megadevelopers.nanogram.solver.Solver;

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
        Board board = new Board(puzzle.height(), puzzle.width());

        Board result = solve(board, puzzle);
        if (result == null) return new SolveResult.NoSolution();

        Cell[][] solved = result.snapshot();
        emit(solved, listener);
        return new SolveResult.Solved(solved);
    }

    private Board solve(Board board, Puzzle puzzle) {
        if (!propagateToFixedPoint(board, puzzle)) return null;
        if (board.isFullyDetermined()) return board;

        GuessCell guess = findMostConstrainedCell(board, puzzle);

        Board withFilled = board.copy();
        withFilled.set(guess.row(), guess.column(), Cell.FILLED);
        Board result = solve(withFilled, puzzle);
        if (result != null) return result;

        Board withEmpty = board.copy();
        withEmpty.set(guess.row(), guess.column(), Cell.EMPTY);
        return solve(withEmpty, puzzle);
    }

    private static boolean propagateToFixedPoint(Board board, Puzzle puzzle) {
        boolean changed;
        do {
            changed = false;
            for (int row = 0; row < puzzle.height(); row++) {
                LineView line = board.row(row);
                Cell[] forced = LineForcedCellSolver.determineForced(line, puzzle.rowClues().get(row));
                if (forced == null) return false;
                if (apply(line, forced)) changed = true;
            }
            for (int column = 0; column < puzzle.width(); column++) {
                LineView line = board.column(column);
                Cell[] forced = LineForcedCellSolver.determineForced(line, puzzle.columnClues().get(column));
                if (forced == null) return false;
                if (apply(line, forced)) changed = true;
            }
        } while (changed);
        return true;
    }

    private static boolean apply(LineView line, Cell[] forced) {
        boolean changed = false;
        for (int i = 0; i < forced.length; i++) {
            if (forced[i] != Cell.NO_VALUE && line.get(i) == Cell.NO_VALUE) {
                line.set(i, forced[i]);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * The undetermined cell belonging to whichever row or column has the
     * fewest remaining undetermined cells (ties broken by preferring rows,
     * then by increasing index) - the DP-world analogue of the
     * fewest-remaining-candidates heuristic HybridSolver uses.
     */
    private static GuessCell findMostConstrainedCell(Board board, Puzzle puzzle) {
        int bestRow = -1, bestRowCount = Integer.MAX_VALUE;
        for (int row = 0; row < puzzle.height(); row++) {
            int count = countUndetermined(board.row(row));
            if (count > 0 && count < bestRowCount) {
                bestRow = row;
                bestRowCount = count;
            }
        }

        int bestColumn = -1, bestColumnCount = Integer.MAX_VALUE;
        for (int column = 0; column < puzzle.width(); column++) {
            int count = countUndetermined(board.column(column));
            if (count > 0 && count < bestColumnCount) {
                bestColumn = column;
                bestColumnCount = count;
            }
        }

        if (bestRow >= 0 && (bestColumn < 0 || bestRowCount <= bestColumnCount)) {
            return new GuessCell(bestRow, firstUndetermined(board.row(bestRow)));
        }
        return new GuessCell(firstUndetermined(board.column(bestColumn)), bestColumn);
    }

    private static int countUndetermined(LineView line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.get(i) == Cell.NO_VALUE) count++;
        }
        return count;
    }

    private static int firstUndetermined(LineView line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.get(i) == Cell.NO_VALUE) return i;
        }
        throw new IllegalStateException("line has no undetermined cell");
    }

    private static void emit(Cell[][] board, CellListener listener) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                listener.onCellChanged(row, column, board[row][column]);
            }
        }
    }
}

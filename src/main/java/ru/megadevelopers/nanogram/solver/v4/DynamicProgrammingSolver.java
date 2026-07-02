package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import static ru.megadevelopers.nanogram.model.Cell.*;
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

        result.emitTo(listener);
        return new SolveResult.Solved(result.snapshot());
    }

    private Board solve(Board board, Puzzle puzzle) {
        if (!propagateToFixedPoint(board, puzzle)) return null;
        if (board.isFullyDetermined()) return board;

        GuessCell guess = findMostConstrainedCell(board, puzzle);

        Board withFilled = board.copy();
        withFilled.set(guess.row(), guess.column(), FILLED);
        Board result = solve(withFilled, puzzle);
        if (result != null) return result;

        Board withEmpty = board.copy();
        withEmpty.set(guess.row(), guess.column(), EMPTY);
        return solve(withEmpty, puzzle);
    }

    private static boolean propagateToFixedPoint(Board board, Puzzle puzzle) {
        boolean changed;
        do {
            changed = false;
            for (int row = 0; row < board.rowsCount(); row++) {
                LineView line = board.row(row);
                Cell[] forced = LineForcedCellSolver.determineForced(line, puzzle.rowClues().get(row));
                if (forced == null) return false;
                if (line.applyForced(forced)) changed = true;
            }
            for (int column = 0; column < board.columnCount(); column++) {
                LineView line = board.column(column);
                Cell[] forced = LineForcedCellSolver.determineForced(line, puzzle.columnClues().get(column));
                if (forced == null) return false;
                if (line.applyForced(forced)) changed = true;
            }
        } while (changed);
        return true;
    }

    /**
     * The undetermined cell belonging to whichever row or column has the
     * fewest remaining undetermined cells (ties broken by preferring rows,
     * then by increasing index) - the DP-world analogue of the
     * fewest-remaining-candidates heuristic HybridSolver uses.
     */
    private static GuessCell findMostConstrainedCell(Board board, Puzzle puzzle) {
        int bestRow = -1, bestRowCount = Integer.MAX_VALUE;
        for (int row = 0; row < board.rowsCount(); row++) {
            int count = board.row(row).countUndetermined();
            if (count > 0 && count < bestRowCount) {
                bestRow = row;
                bestRowCount = count;
            }
        }

        int bestColumn = -1, bestColumnCount = Integer.MAX_VALUE;
        for (int column = 0; column < board.columnCount(); column++) {
            int count = board.column(column).countUndetermined();
            if (count > 0 && count < bestColumnCount) {
                bestColumn = column;
                bestColumnCount = count;
            }
        }

        if (bestRow >= 0 && (bestColumn < 0 || bestRowCount <= bestColumnCount)) {
            return new GuessCell(bestRow, board.row(bestRow).firstUndetermined());
        }
        return new GuessCell(board.column(bestColumn).firstUndetermined(), bestColumn);
    }
}

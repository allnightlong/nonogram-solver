package ru.megadevelopers.nanogram.solver.support;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.model.Line;
import ru.megadevelopers.nanogram.solver.Puzzle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Row/column constraint propagation shared by v1's PropagationSolver and
 * v3's HybridSolver. Tracks, per row and per column, the set of candidate
 * placements still consistent with every other line's candidates, and
 * narrows those sets until a fixed point (or a contradiction) is reached.
 */
public class LineConstraintPropagator {

    public record GuessCell(int row, int column) {}

    private final int width;
    private final int height;
    private final List<LineCandidates> rowCandidates;
    private final List<LineCandidates> columnCandidates;

    public LineConstraintPropagator(Puzzle puzzle) {
        this.width = puzzle.width();
        this.height = puzzle.height();
        this.rowCandidates = new ArrayList<>();
        for (Clue clue : puzzle.rowClues()) {
            rowCandidates.add(new LineCandidates(Line.candidates(clue, width)));
        }
        this.columnCandidates = new ArrayList<>();
        for (Clue clue : puzzle.columnClues()) {
            columnCandidates.add(new LineCandidates(Line.candidates(clue, height)));
        }
    }

    private LineConstraintPropagator(int width, int height,
                                      List<LineCandidates> rowCandidates, List<LineCandidates> columnCandidates) {
        this.width = width;
        this.height = height;
        this.rowCandidates = deepCopy(rowCandidates);
        this.columnCandidates = deepCopy(columnCandidates);
    }

    public LineConstraintPropagator copy() {
        return new LineConstraintPropagator(width, height, rowCandidates, columnCandidates);
    }

    /** Propagates constraints to a fixed point. Returns false on contradiction (no solution). */
    public boolean propagateToFixedPoint() {
        int changed;
        do {
            changed = reduceMutual();
            if (changed == -1) return false;
        } while (changed > 0);
        return true;
    }

    public boolean isSolved() {
        return rowCandidates.stream().allMatch(c -> c.size() == 1)
                && columnCandidates.stream().allMatch(c -> c.size() == 1);
    }

    public Cell[][] extractBoard() {
        Cell[][] board = new Cell[height][width];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                board[row][column] = rowCandidates.get(row).cellAt(column);
            }
        }
        return board;
    }

    /**
     * The undetermined cell belonging to whichever row or column has the
     * fewest remaining candidates (ties broken by preferring rows, then by
     * increasing index) - the standard "most constrained variable" choice
     * for the next guess.
     */
    public GuessCell findMostConstrainedCell() {
        int bestRow = -1, bestRowSize = Integer.MAX_VALUE;
        for (int row = 0; row < rowCandidates.size(); row++) {
            int size = rowCandidates.get(row).size();
            if (size > 1 && size < bestRowSize) {
                bestRow = row;
                bestRowSize = size;
            }
        }

        int bestColumn = -1, bestColumnSize = Integer.MAX_VALUE;
        for (int column = 0; column < columnCandidates.size(); column++) {
            int size = columnCandidates.get(column).size();
            if (size > 1 && size < bestColumnSize) {
                bestColumn = column;
                bestColumnSize = size;
            }
        }

        if (bestRow >= 0 && (bestColumn < 0 || bestRowSize <= bestColumnSize)) {
            return new GuessCell(bestRow, rowCandidates.get(bestRow).firstUndetermined(width));
        }
        return new GuessCell(columnCandidates.get(bestColumn).firstUndetermined(height), bestColumn);
    }

    /** Restricts the given cell to the given value for a guess. Returns false if that's an immediate contradiction. */
    public boolean restrictCell(GuessCell guess, boolean filled) {
        LineCandidates candidates = rowCandidates.get(guess.row());
        candidates.removeIf(candidate -> candidate.get(guess.column()) != filled);
        return !candidates.isEmpty();
    }

    private int reduceMutual() {
        int removedFromColumns = reduce(rowCandidates, columnCandidates);
        if (removedFromColumns == -1) return -1;

        int removedFromRows = reduce(columnCandidates, rowCandidates);
        if (removedFromRows == -1) return -1;

        return removedFromColumns + removedFromRows;
    }

    private static int reduce(List<LineCandidates> a, List<LineCandidates> b) {
        int countRemoved = 0;

        for (int i = 0; i < a.size(); i++) {
            BitSet commonOn = new BitSet();
            commonOn.set(0, b.size());
            BitSet commonOff = new BitSet();

            for (BitSet candidate : a.get(i)) {
                commonOn.and(candidate);
                commonOff.or(candidate);
            }

            for (int j = 0; j < b.size(); j++) {
                int fi = i, fj = j;

                if (b.get(j).removeIf(cnd -> (commonOn.get(fj) && !cnd.get(fi))
                        || (!commonOff.get(fj) && cnd.get(fi))))
                    countRemoved++;

                if (b.get(j).isEmpty()) return -1;
            }
        }
        return countRemoved;
    }

    private static List<LineCandidates> deepCopy(List<LineCandidates> source) {
        List<LineCandidates> copy = new ArrayList<>();
        for (LineCandidates line : source) {
            copy.add(line.copy());
        }
        return copy;
    }
}

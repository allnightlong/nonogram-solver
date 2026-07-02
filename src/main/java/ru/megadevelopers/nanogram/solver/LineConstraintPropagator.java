package ru.megadevelopers.nanogram.solver;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Line;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Row/column constraint propagation shared by PropagationSolver and HybridSolver.
 * Tracks, per row and per column, the set of candidate placements still
 * consistent with every other line's candidates, and narrows those sets
 * until a fixed point (or a contradiction) is reached.
 */
class LineConstraintPropagator {

    record GuessCell(int row, int column) {}

    private final int width;
    private final int height;
    private final List<List<BitSet>> rowCandidates;
    private final List<List<BitSet>> columnCandidates;

    LineConstraintPropagator(Puzzle puzzle) {
        this.width = puzzle.width();
        this.height = puzzle.height();
        this.rowCandidates = new ArrayList<>();
        for (List<Integer> clue : puzzle.rowClues()) {
            rowCandidates.add(new ArrayList<>(Line.candidates(clue, width)));
        }
        this.columnCandidates = new ArrayList<>();
        for (List<Integer> clue : puzzle.columnClues()) {
            columnCandidates.add(new ArrayList<>(Line.candidates(clue, height)));
        }
    }

    private LineConstraintPropagator(int width, int height,
                                      List<List<BitSet>> rowCandidates, List<List<BitSet>> columnCandidates) {
        this.width = width;
        this.height = height;
        this.rowCandidates = deepCopy(rowCandidates);
        this.columnCandidates = deepCopy(columnCandidates);
    }

    LineConstraintPropagator copy() {
        return new LineConstraintPropagator(width, height, rowCandidates, columnCandidates);
    }

    /** Propagates constraints to a fixed point. Returns false on contradiction (no solution). */
    boolean propagateToFixedPoint() {
        int changed;
        do {
            changed = reduceMutual();
            if (changed == -1) return false;
        } while (changed > 0);
        return true;
    }

    boolean isSolved() {
        return rowCandidates.stream().allMatch(c -> c.size() == 1)
                && columnCandidates.stream().allMatch(c -> c.size() == 1);
    }

    Cell[][] extractBoard() {
        Cell[][] board = new Cell[height][width];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                board[row][column] = cellAt(rowCandidates.get(row), column);
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
    GuessCell findMostConstrainedCell() {
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
            return new GuessCell(bestRow, firstUndetermined(rowCandidates.get(bestRow), width));
        }
        return new GuessCell(firstUndetermined(columnCandidates.get(bestColumn), height), bestColumn);
    }

    /** Restricts the given cell to the given value for a guess. Returns false if that's an immediate contradiction. */
    boolean restrictCell(GuessCell guess, boolean filled) {
        List<BitSet> candidates = rowCandidates.get(guess.row());
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

    private static int reduce(List<List<BitSet>> a, List<List<BitSet>> b) {
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

    private static Cell cellAt(List<BitSet> candidates, int index) {
        if (!allAgree(candidates, index)) return Cell.NO_VALUE;
        return candidates.get(0).get(index) ? Cell.FILLED : Cell.EMPTY;
    }

    private static int firstUndetermined(List<BitSet> candidates, int bound) {
        for (int i = 0; i < bound; i++) {
            if (!allAgree(candidates, i)) return i;
        }
        throw new IllegalStateException("line has no undetermined cell");
    }

    private static boolean allAgree(List<BitSet> candidates, int index) {
        boolean first = candidates.get(0).get(index);
        for (BitSet candidate : candidates) {
            if (candidate.get(index) != first) return false;
        }
        return true;
    }

    private static List<List<BitSet>> deepCopy(List<List<BitSet>> source) {
        List<List<BitSet>> copy = new ArrayList<>();
        for (List<BitSet> line : source) {
            List<BitSet> lineCopy = new ArrayList<>();
            for (BitSet candidate : line) {
                lineCopy.add((BitSet) candidate.clone());
            }
            copy.add(lineCopy);
        }
        return copy;
    }
}

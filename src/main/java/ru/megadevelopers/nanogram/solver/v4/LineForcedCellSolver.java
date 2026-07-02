package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;

import java.util.Arrays;
import java.util.List;

/**
 * Determines, for a single line, which cells are forced FILLED or EMPTY
 * given the clue and the line's current (possibly partially known) state.
 * Sweeps every block against every candidate start position using
 * LineFeasibility (Steve Simpson's classic DP feasibility technique) to
 * find each block's leftmost/rightmost feasible placement, then derives
 * forced cells from the overlap - never enumerating candidate placements.
 */
class LineForcedCellSolver {

    /** Forced values per cell (NO_VALUE where still undetermined), or null if infeasible. */
    static Cell[] determineForced(LineView line, Clue clue) {
        List<Integer> blockLengths = clue.blockLengths();
        int lineLength = line.length();
        int blockCount = blockLengths.size();

        LineFeasibility feasibility = LineFeasibility.analyze(line, blockLengths);
        if (!feasibility.isFullyFeasible()) return null;

        Cell[] forced = new Cell[lineLength];
        Arrays.fill(forced, Cell.NO_VALUE);

        if (blockCount == 0) {
            Arrays.fill(forced, Cell.EMPTY);
            return forced;
        }

        boolean[] possiblyFilledCells = new boolean[lineLength];

        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            int blockLength = blockLengths.get(blockIndex);
            int leftmostFeasibleStart = -1, rightmostFeasibleStart = -1;

            for (int candidateStart = 0; candidateStart + blockLength <= lineLength; candidateStart++) {
                if (feasibility.canPlaceBlockAt(blockIndex, candidateStart, blockLength)) {
                    if (leftmostFeasibleStart == -1) leftmostFeasibleStart = candidateStart;
                    rightmostFeasibleStart = candidateStart;
                }
            }

            for (int position = leftmostFeasibleStart; position < rightmostFeasibleStart + blockLength; position++) {
                possiblyFilledCells[position] = true;
            }
            if (rightmostFeasibleStart < leftmostFeasibleStart + blockLength) {
                for (int position = rightmostFeasibleStart; position < leftmostFeasibleStart + blockLength; position++) {
                    forced[position] = Cell.FILLED;
                }
            }
        }

        for (int position = 0; position < lineLength; position++) {
            if (!possiblyFilledCells[position]) forced[position] = Cell.EMPTY;
        }

        return forced;
    }
}

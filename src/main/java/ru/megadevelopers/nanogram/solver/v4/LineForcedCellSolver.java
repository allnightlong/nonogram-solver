package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Block;
import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;

import java.util.Arrays;
import java.util.List;

import static ru.megadevelopers.nanogram.model.Cell.*;

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
        List<Block> blocks = clue.blocks();
        int lineLength = line.length();

        LineFeasibility feasibility = LineFeasibility.analyze(line, blocks);
        if (!feasibility.isFullyFeasible()) return null;

        Cell[] forced = new Cell[lineLength];
        Arrays.fill(forced, NO_VALUE);

        if (blocks.isEmpty()) {
            Arrays.fill(forced, EMPTY);
            return forced;
        }

        boolean[] possiblyFilledCells = new boolean[lineLength];

        for (Block block : blocks) {
            int leftmostFeasibleStart = -1, rightmostFeasibleStart = -1;

            for (int blockStart = 0; blockStart + block.length() <= lineLength; blockStart++) {
                if (feasibility.isFeasibleAt(block, blockStart)) {
                    if (leftmostFeasibleStart == -1) leftmostFeasibleStart = blockStart;
                    rightmostFeasibleStart = blockStart;
                }
            }

            for (int position = leftmostFeasibleStart; position < rightmostFeasibleStart + block.length(); position++) {
                possiblyFilledCells[position] = true;
            }
            if (rightmostFeasibleStart < leftmostFeasibleStart + block.length()) {
                for (int position = rightmostFeasibleStart; position < leftmostFeasibleStart + block.length(); position++) {
                    forced[position] = FILLED;
                }
            }
        }

        for (int position = 0; position < lineLength; position++) {
            if (!possiblyFilledCells[position]) forced[position] = EMPTY;
        }

        return forced;
    }
}

package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.Clue;

import java.util.Arrays;

/**
 * Determines, for a single line, which cells are forced FILLED or EMPTY
 * given the clue and the line's current (possibly partially known) state -
 * via two DP feasibility passes (leftmost-fit / rightmost-fit), never
 * enumerating candidate placements. Steve Simpson's classic nonogram
 * line-solving technique: cost is O(length x blocks), independent of how
 * many valid placements exist.
 */
class LineForcedCellSolver {

    /** Forced values per cell (NO_VALUE where still undetermined), or null if infeasible. */
    static Cell[] determineForced(LineView line, Clue clue) {
        int[] blockLengths = clue.blockLengths().stream().mapToInt(Integer::intValue).toArray();
        int lineLength = line.length();
        int blockCount = blockLengths.length;

        FeasibilityTable prefixFeasibility = computePrefixFeasibility(line, blockLengths);
        if (!prefixFeasibility.isFeasible(lineLength, blockCount)) return null;

        Cell[] forced = new Cell[lineLength];
        Arrays.fill(forced, Cell.NO_VALUE);

        if (blockCount == 0) {
            Arrays.fill(forced, Cell.EMPTY);
            return forced;
        }

        FeasibilityTable reversedPrefixFeasibility =
                computePrefixFeasibility(reverse(line), reverseBlockLengths(blockLengths));
        boolean[] possiblyFilledCells = new boolean[lineLength];

        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            int blockLength = blockLengths[blockIndex];
            int leftmostFeasibleStart = -1, rightmostFeasibleStart = -1;

            for (int candidateStart = 0; candidateStart + blockLength <= lineLength; candidateStart++) {
                if (canPlaceBlockAt(line, prefixFeasibility, reversedPrefixFeasibility,
                        lineLength, blockCount, blockIndex, candidateStart, blockLength)) {
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

    private static boolean canPlaceBlockAt(LineView line, FeasibilityTable prefixFeasibility, FeasibilityTable reversedPrefixFeasibility,
                                            int lineLength, int blockCount, int blockIndex, int candidateStart, int blockLength) {
        if (!isPrefixFeasible(line, prefixFeasibility, candidateStart, blockIndex)) return false;
        if (!isRangeCompatible(line, candidateStart, candidateStart + blockLength, Cell.FILLED)) return false;
        return isSuffixFeasible(line, reversedPrefixFeasibility, lineLength, blockCount,
                candidateStart + blockLength, blockIndex);
    }

    /** Can the blocks before {@code blockIndex} fit in the line before {@code candidateStart}? */
    private static boolean isPrefixFeasible(LineView line, FeasibilityTable prefixFeasibility, int candidateStart, int blockIndex) {
        if (blockIndex == 0) return prefixFeasibility.isFeasible(candidateStart, 0);
        return candidateStart >= 1
                && isCompatible(line.get(candidateStart - 1), Cell.EMPTY)
                && prefixFeasibility.isFeasible(candidateStart - 1, blockIndex);
    }

    /** Can the blocks after {@code blockIndex} fit in the line from {@code blockEnd} onward? */
    private static boolean isSuffixFeasible(LineView line, FeasibilityTable reversedPrefixFeasibility,
                                             int lineLength, int blockCount, int blockEnd, int blockIndex) {
        if (blockIndex == blockCount - 1) {
            return suffixFeasibleFromReversedTable(reversedPrefixFeasibility, lineLength, blockCount, blockEnd, blockCount);
        }
        return blockEnd < lineLength
                && isCompatible(line.get(blockEnd), Cell.EMPTY)
                && suffixFeasibleFromReversedTable(reversedPrefixFeasibility, lineLength, blockCount, blockEnd + 1, blockIndex + 1);
    }

    /**
     * Looks up suffix feasibility using the prefix-feasibility table computed
     * on the reversed line: a suffix starting at {@code originalPrefixLength}
     * with {@code originalBlocksPlaced} blocks already placed corresponds to
     * a prefix of the reversed line/blocks of the mirrored size.
     */
    private static boolean suffixFeasibleFromReversedTable(FeasibilityTable reversedPrefixFeasibility,
                                                             int lineLength, int blockCount,
                                                             int originalPrefixLength, int originalBlocksPlaced) {
        return reversedPrefixFeasibility.isFeasible(lineLength - originalPrefixLength, blockCount - originalBlocksPlaced);
    }

    /**
     * prefixFeasibility[prefixLength][blocksPlaced] = true iff the first
     * blocksPlaced blocks can be placed within the first prefixLength cells
     * of the line, consistent with any already-known cells. Two transitions
     * per state: extend the gap by one cell, or end the next block exactly
     * at the end of this prefix (with its mandatory single-cell gap before
     * it, unless it's the first block).
     */
    private static FeasibilityTable computePrefixFeasibility(LineView line, int[] blockLengths) {
        int lineLength = line.length();
        int blockCount = blockLengths.length;
        FeasibilityTable feasibilityTable = new FeasibilityTable(lineLength, blockCount);
        feasibilityTable.markFeasible(0, 0, true);

        for (int prefixLength = 1; prefixLength <= lineLength; prefixLength++) {
            feasibilityTable.markFeasible(prefixLength, 0,
                    feasibilityTable.isFeasible(prefixLength - 1, 0) && isCompatible(line.get(prefixLength - 1), Cell.EMPTY));
        }

        for (int blocksPlaced = 1; blocksPlaced <= blockCount; blocksPlaced++) {
            int blockLength = blockLengths[blocksPlaced - 1];
            for (int prefixLength = 1; prefixLength <= lineLength; prefixLength++) {
                boolean canFit = feasibilityTable.isFeasible(prefixLength - 1, blocksPlaced)
                        && isCompatible(line.get(prefixLength - 1), Cell.EMPTY);

                if (!canFit && prefixLength >= blockLength) {
                    int blockStart = prefixLength - blockLength;
                    if (isRangeCompatible(line, blockStart, prefixLength, Cell.FILLED)) {
                        if (blocksPlaced == 1) {
                            canFit = feasibilityTable.isFeasible(blockStart, 0);
                        } else {
                            canFit = blockStart >= 1
                                    && isCompatible(line.get(blockStart - 1), Cell.EMPTY)
                                    && feasibilityTable.isFeasible(blockStart - 1, blocksPlaced - 1);
                        }
                    }
                }
                feasibilityTable.markFeasible(prefixLength, blocksPlaced, canFit);
            }
        }
        return feasibilityTable;
    }

    private static boolean isRangeCompatible(LineView line, int from, int to, Cell desired) {
        for (int position = from; position < to; position++) {
            if (!isCompatible(line.get(position), desired)) return false;
        }
        return true;
    }

    private static boolean isCompatible(Cell cell, Cell desired) {
        return cell == Cell.NO_VALUE || cell == desired;
    }

    private static LineView reverse(LineView line) {
        return new LineView() {
            public int length() {
                return line.length();
            }

            public Cell get(int index) {
                return line.get(line.length() - 1 - index);
            }

            public void set(int index, Cell value) {
                line.set(line.length() - 1 - index, value);
            }
        };
    }

    private static int[] reverseBlockLengths(int[] blockLengths) {
        int[] result = new int[blockLengths.length];
        for (int i = 0; i < blockLengths.length; i++) result[i] = blockLengths[blockLengths.length - 1 - i];
        return result;
    }
}

package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Block;
import ru.megadevelopers.nanogram.model.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Answers feasibility questions for one line against one clue - "is the
 * whole line feasible at all," and "is this block feasible starting at
 * this position" - by combining a forward pass (earlier blocks must fit
 * before that position) with a backward pass computed on the reversed line
 * (later blocks must fit after it ends). Steve Simpson's classic nonogram
 * line-solving technique: cost is O(length x blocks), independent of how
 * many valid placements exist. Built once per line, then queried per
 * (block, start position) pair during the sweep in LineForcedCellSolver.
 */
class LineFeasibility {

    private final LineView line;
    private final int lineLength;
    private final int blockCount;
    private final FeasibilityTable prefixFeasibility;
    private final FeasibilityTable reversedPrefixFeasibility;

    private LineFeasibility(LineView line, List<Block> blocks) {
        this.line = line;
        this.lineLength = line.length();
        this.blockCount = blocks.size();
        List<Integer> blockLengths = blocks.stream().map(Block::length).toList();
        this.prefixFeasibility = computePrefixFeasibility(line, blockLengths);
        this.reversedPrefixFeasibility = computePrefixFeasibility(line.reversed(), reverseBlockLengths(blockLengths));
    }

    /** Computes both feasibility tables for this line against these blocks - O(length x blocks) work. */
    static LineFeasibility analyze(LineView line, List<Block> blocks) {
        return new LineFeasibility(line, blocks);
    }

    boolean isFullyFeasible() {
        return prefixFeasibility.isFeasible(lineLength, blockCount);
    }

    boolean isFeasibleAt(Block block, int blockStart) {
        if (!isPrefixFeasible(blockStart, block.index())) return false;
        if (!isRangeCompatible(line, blockStart, blockStart + block.length(), Cell.FILLED)) return false;
        return isSuffixFeasible(blockStart + block.length(), block.index());
    }

    /** Can the blocks before {@code blockIndex} fit in the line before {@code blockStart}? */
    private boolean isPrefixFeasible(int blockStart, int blockIndex) {
        if (blockIndex == 0) return prefixFeasibility.isFeasible(blockStart, 0);
        return blockStart >= 1
                && isCompatible(line.get(blockStart - 1), Cell.EMPTY)
                && prefixFeasibility.isFeasible(blockStart - 1, blockIndex);
    }

    /** Can the blocks after {@code blockIndex} fit in the line from {@code blockEnd} onward? */
    private boolean isSuffixFeasible(int blockEnd, int blockIndex) {
        if (blockIndex == blockCount - 1) {
            return suffixFeasibleFromReversedTable(blockEnd, blockCount);
        }
        return blockEnd < lineLength
                && isCompatible(line.get(blockEnd), Cell.EMPTY)
                && suffixFeasibleFromReversedTable(blockEnd + 1, blockIndex + 1);
    }

    /**
     * Looks up suffix feasibility using the prefix-feasibility table
     * computed on the reversed line: a suffix starting at
     * {@code originalPrefixLength} with {@code originalBlocksPlaced}
     * blocks already placed corresponds to a prefix of the reversed
     * line/blocks of the mirrored size.
     */
    private boolean suffixFeasibleFromReversedTable(int originalPrefixLength, int originalBlocksPlaced) {
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
    private static FeasibilityTable computePrefixFeasibility(LineView line, List<Integer> blockLengths) {
        int lineLength = line.length();
        int blockCount = blockLengths.size();
        FeasibilityTable feasibilityTable = new FeasibilityTable(lineLength, blockCount);
        feasibilityTable.markFeasible(0, 0, true);

        for (int prefixLength = 1; prefixLength <= lineLength; prefixLength++) {
            feasibilityTable.markFeasible(prefixLength, 0,
                    feasibilityTable.isFeasible(prefixLength - 1, 0) && isCompatible(line.get(prefixLength - 1), Cell.EMPTY));
        }

        for (int blocksPlaced = 1; blocksPlaced <= blockCount; blocksPlaced++) {
            int blockLength = blockLengths.get(blocksPlaced - 1);
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

    private static List<Integer> reverseBlockLengths(List<Integer> blockLengths) {
        List<Integer> result = new ArrayList<>(blockLengths);
        Collections.reverse(result);
        return result;
    }
}

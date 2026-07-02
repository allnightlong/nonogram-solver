package ru.megadevelopers.nanogram.solver.v4;

/**
 * isFeasible(prefixLength, blocksPlaced) = true iff the first blocksPlaced
 * blocks of a clue can be placed within the first prefixLength cells of a
 * line, consistent with whatever's already known about that line. Sized
 * (lineLength + 1) x (blockCount + 1) since both indices are counts that
 * range from 0 (nothing considered yet) up to and including the total.
 */
class FeasibilityTable {

    private final boolean[][] feasible;

    FeasibilityTable(int lineLength, int blockCount) {
        this.feasible = new boolean[lineLength + 1][blockCount + 1];
    }

    boolean isFeasible(int prefixLength, int blocksPlaced) {
        return feasible[prefixLength][blocksPlaced];
    }

    void markFeasible(int prefixLength, int blocksPlaced, boolean value) {
        feasible[prefixLength][blocksPlaced] = value;
    }
}

package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;

import java.util.Arrays;
import java.util.List;

/**
 * Determines, for a single line, which cells are forced FILLED or EMPTY
 * given the clue and the line's current (possibly partially known) state -
 * via two DP feasibility passes (leftmost-fit / rightmost-fit), never
 * enumerating candidate placements. Steve Simpson's classic nonogram
 * line-solving technique: cost is O(length x blocks), independent of how
 * many valid placements exist.
 */
class LineDpSolver {

    /** Forced values per cell (NO_VALUE where still undetermined), or null if infeasible. */
    static Cell[] determineForced(Cell[] line, List<Integer> clue) {
        int[] blocks = clue.stream().filter(v -> v != 0).mapToInt(Integer::intValue).toArray();
        int n = line.length;
        int k = blocks.length;

        boolean[][] forward = reachability(line, blocks);
        if (!forward[n][k]) return null;

        Cell[] forced = new Cell[n];
        Arrays.fill(forced, Cell.NO_VALUE);

        if (k == 0) {
            Arrays.fill(forced, Cell.EMPTY);
            return forced;
        }

        boolean[][] backwardRev = reachability(reverse(line), reverseBlocks(blocks));
        boolean[] reachableUnion = new boolean[n];

        for (int b = 0; b < k; b++) {
            int len = blocks[b];
            int minStart = -1, maxStart = -1;

            for (int s = 0; s + len <= n; s++) {
                if (feasible(line, forward, backwardRev, n, k, b, s, len)) {
                    if (minStart == -1) minStart = s;
                    maxStart = s;
                }
            }

            for (int p = minStart; p < maxStart + len; p++) {
                reachableUnion[p] = true;
            }
            if (maxStart < minStart + len) {
                for (int p = maxStart; p < minStart + len; p++) {
                    forced[p] = Cell.FILLED;
                }
            }
        }

        for (int p = 0; p < n; p++) {
            if (!reachableUnion[p]) forced[p] = Cell.EMPTY;
        }

        return forced;
    }

    private static boolean feasible(Cell[] line, boolean[][] forward, boolean[][] backwardRev,
                                     int n, int k, int b, int s, int len) {
        if (!prefixOk(line, forward, s, b)) return false;
        if (!allCompatible(line, s, s + len, true)) return false;
        return suffixOk(line, backwardRev, n, k, s + len, b);
    }

    private static boolean prefixOk(Cell[] line, boolean[][] forward, int s, int b) {
        if (b == 0) return forward[s][0];
        return s >= 1 && compatible(line[s - 1], false) && forward[s - 1][b];
    }

    private static boolean suffixOk(Cell[] line, boolean[][] backwardRev, int n, int k, int e, int b) {
        if (b == k - 1) return backwardAt(backwardRev, n, k, e, k);
        return e < n && compatible(line[e], false) && backwardAt(backwardRev, n, k, e + 1, b + 1);
    }

    private static boolean backwardAt(boolean[][] backwardRev, int n, int k, int i, int j) {
        return backwardRev[n - i][k - j];
    }

    /**
     * reachability[i][j] = true iff the first j blocks can be placed within
     * the first i cells of the line, consistent with any already-known
     * cells. Two transitions per (i,j): extend the gap by one cell, or end
     * block j-1 exactly at position i-1 (with its mandatory single-cell
     * gap before it, unless it's the first block).
     */
    private static boolean[][] reachability(Cell[] line, int[] blocks) {
        int n = line.length;
        int k = blocks.length;
        boolean[][] r = new boolean[n + 1][k + 1];
        r[0][0] = true;

        for (int i = 1; i <= n; i++) {
            r[i][0] = r[i - 1][0] && compatible(line[i - 1], false);
        }

        for (int j = 1; j <= k; j++) {
            int len = blocks[j - 1];
            for (int i = 1; i <= n; i++) {
                boolean value = r[i - 1][j] && compatible(line[i - 1], false);

                if (!value && i >= len) {
                    int start = i - len;
                    if (allCompatible(line, start, i, true)) {
                        if (j == 1) {
                            value = r[start][0];
                        } else {
                            value = start >= 1 && compatible(line[start - 1], false) && r[start - 1][j - 1];
                        }
                    }
                }
                r[i][j] = value;
            }
        }
        return r;
    }

    private static boolean allCompatible(Cell[] line, int from, int to, boolean wantFilled) {
        for (int p = from; p < to; p++) {
            if (!compatible(line[p], wantFilled)) return false;
        }
        return true;
    }

    private static boolean compatible(Cell cell, boolean wantFilled) {
        return wantFilled ? cell != Cell.EMPTY : cell != Cell.FILLED;
    }

    private static Cell[] reverse(Cell[] line) {
        Cell[] result = new Cell[line.length];
        for (int i = 0; i < line.length; i++) result[i] = line[line.length - 1 - i];
        return result;
    }

    private static int[] reverseBlocks(int[] blocks) {
        int[] result = new int[blocks.length];
        for (int i = 0; i < blocks.length; i++) result[i] = blocks[blocks.length - 1 - i];
        return result;
    }
}

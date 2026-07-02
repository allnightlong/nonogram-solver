package ru.megadevelopers.nanogram.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static ru.megadevelopers.nanogram.model.Cell.*;

public class Line {

    public static boolean isValid(List<Cell> line, List<BitSet> candidates) {
        return candidates.stream().anyMatch(c -> isValid(line, c));
    }

    public static boolean isValid(List<Cell> line, BitSet candidate) {
        for (int index = 0; index < line.size(); index++) {
            Cell cell = line.get(index);
            if (cell == EMPTY && candidate.get(index)) return false;
            if (cell == FILLED && !candidate.get(index)) return false;
        }
        return true;
    }

    public static List<BitSet> candidates(Clue clue, int length) {
        List<Integer> blocks = clue.blockLengths();
        int totalFilled = blocks.stream().mapToInt(Integer::intValue).sum();
        int slack = length - totalFilled - Math.max(blocks.size() - 1, 0);

        List<BitSet> result = new ArrayList<>();
        place(blocks, 0, slack, 0, new BitSet(length), result);
        return result;
    }

    /**
     * Places each remaining block at every valid offset, sharing one mutable
     * BitSet across the whole search (set the block's bits, recurse, clear
     * them again) so each output candidate costs exactly one clone - gaps
     * are free since a BitSet already starts all-clear, and there's no
     * intermediate String representation to build or re-parse.
     */
    private static void place(List<Integer> blocks, int blockIndex, int remainingSlack,
                               int position, BitSet current, List<BitSet> result) {
        if (blockIndex == blocks.size()) {
            result.add((BitSet) current.clone());
            return;
        }

        int minGap = blockIndex == 0 ? 0 : 1;
        int blockLength = blocks.get(blockIndex);

        for (int extra = 0; extra <= remainingSlack; extra++) {
            int start = position + minGap + extra;
            current.set(start, start + blockLength);
            place(blocks, blockIndex + 1, remainingSlack - extra, start + blockLength, current, result);
            current.clear(start, start + blockLength);
        }
    }

    static List<BitSet> bitSetList(List<List<Integer>> source) {
        return source.stream().map(Line::toBitSet).toList();
    }

    static BitSet toBitSet(List<Integer> list) {
        BitSet set = new BitSet();
        for (int i = 0; i < list.size(); i++) {
            set.set(i, list.get(i) != 0);
        }
        return set;
    }
}

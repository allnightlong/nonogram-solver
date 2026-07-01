package ru.megadevelopers.nanogram.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Line {

    private static final char EMPTY_CHAR = '0';
    private static final char FILLED_CHAR = '1';

    public static boolean isValid(List<Cell> line, List<BitSet> candidates) {
        return candidates.stream().anyMatch(c -> isValid(line, c));
    }

    public static boolean isValid(List<Cell> line, BitSet candidate) {
        for (int index = 0; index < line.size(); index++) {
            Cell cell = line.get(index);
            if (cell == Cell.EMPTY && candidate.get(index)) return false;
            if (cell == Cell.FILLED && !candidate.get(index)) return false;
        }
        return true;
    }

    public static List<BitSet> candidates(List<Integer> clues, int length) {
        int totalFilled = clues.stream().mapToInt(Integer::intValue).sum();
        List<String> blocks = clues.stream()
                .filter(v -> v != 0)
                .map(v -> String.valueOf(FILLED_CHAR).repeat(v))
                .toList();
        return generateSequences(blocks, length - totalFilled + 1).stream()
                .map(s -> fromString(s.substring(1)))
                .toList();
    }

    static List<String> generateSequences(List<String> ones, int numZeros) {
        if (ones.isEmpty()) {
            return List.of(String.valueOf(EMPTY_CHAR).repeat(numZeros));
        }
        List<String> sequences = new ArrayList<>();
        for (int x = 1; x < numZeros - ones.size() + 2; x++) {
            for (String tail : generateSequences(ones.subList(1, ones.size()), numZeros - x)) {
                sequences.add(String.valueOf(EMPTY_CHAR).repeat(x) + ones.get(0) + tail);
            }
        }
        return sequences;
    }

    private static BitSet fromString(String binary) {
        BitSet set = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == FILLED_CHAR) set.set(i);
        }
        return set;
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

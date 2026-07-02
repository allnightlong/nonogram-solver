package ru.megadevelopers.nanogram.model;

import java.util.List;

/**
 * One line's clue, as parsed from puzzle data - a sequence of block
 * lengths, possibly containing 0-valued placeholder entries used to pad
 * every clue in a puzzle to a uniform display width (see the JSON source
 * format). {@link #blockLengths()} strips those placeholders to recover
 * the real blocks a solver needs to place.
 */
public record Clue(List<Integer> raw) {

    public static Clue of(Integer... blocks) {
        return new Clue(List.of(blocks));
    }

    public List<Integer> blockLengths() {
        return raw.stream().filter(v -> v != 0).toList();
    }
}

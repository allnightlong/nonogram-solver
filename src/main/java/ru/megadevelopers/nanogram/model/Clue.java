package ru.megadevelopers.nanogram.model;

import java.util.List;

/** One line's clue - the block lengths a solver must place, in order. */
public record Clue(List<Integer> blockLengths) {

    public Clue {
        if (blockLengths.stream().anyMatch(v -> v <= 0)) {
            throw new IllegalArgumentException("Clue block length must be positive: " + blockLengths);
        }
    }

    public static Clue of(Integer... blocks) {
        return new Clue(List.of(blocks));
    }
}

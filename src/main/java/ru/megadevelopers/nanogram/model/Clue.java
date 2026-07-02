package ru.megadevelopers.nanogram.model;

import java.util.List;
import java.util.stream.IntStream;

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

    /** This clue's blocks, each paired with its index among them. */
    public List<Block> blocks() {
        return IntStream.range(0, blockLengths.size())
                .mapToObj(index -> new Block(index, blockLengths.get(index)))
                .toList();
    }
}

package ru.megadevelopers.nanogram.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.megadevelopers.nanogram.model.Clue.clueOf;

class ClueTest {

    @Test
    void blockLengthsAreExposedAsGiven() {
        Clue clue = new Clue(List.of(2, 1));

        assertEquals(List.of(2, 1), clue.blockLengths());
    }

    @Test
    void constructorRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new Clue(List.of(2, -1)));
    }

    @Test
    void constructorRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new Clue(List.of(0, 3)));
    }

    @Test
    void clueOfFactoryRejectsNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> clueOf(2, 0));
        assertThrows(IllegalArgumentException.class, () -> clueOf(2, -1));
    }

    @Test
    void blocksPairEachLengthWithItsIndex() {
        Clue clue = clueOf(2, 1);

        assertEquals(List.of(new Block(0, 2), new Block(1, 1)), clue.blocks());
    }
}

package ru.megadevelopers.nanogram.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClueTest {

    @Test
    void blockLengthsFiltersOutZeroPlaceholders() {
        Clue clue = new Clue(List.of(0, 3));

        assertEquals(List.of(3), clue.blockLengths());
    }

    @Test
    void blockLengthsOfABlankClueIsEmpty() {
        Clue clue = new Clue(List.of(0));

        assertEquals(List.of(), clue.blockLengths());
    }

    @Test
    void constructorRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new Clue(List.of(2, -1)));
    }

    @Test
    void ofFactoryRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Clue.of(2, -1));
    }
}

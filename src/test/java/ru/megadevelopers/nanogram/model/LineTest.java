package ru.megadevelopers.nanogram.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineTest {

    @Test
    void candidates_blockPlusTwoBlock() {
        assertEquals(
            Line.bitSetList(List.of(List.of(1, 0, 1, 1))),
            Line.candidates(List.of(1, 2), 4)
        );
    }

    @Test
    void candidates_singleBlock() {
        assertEquals(
            Line.bitSetList(List.of(
                List.of(1, 0, 0),
                List.of(0, 1, 0),
                List.of(0, 0, 1)
            )),
            Line.candidates(List.of(1), 3)
        );
    }

    @Test
    void candidates_doubleBlock() {
        assertEquals(
            Line.bitSetList(List.of(
                List.of(1, 1, 0, 0, 0),
                List.of(0, 1, 1, 0, 0),
                List.of(0, 0, 1, 1, 0),
                List.of(0, 0, 0, 1, 1)
            )),
            Line.candidates(List.of(2), 5)
        );
    }

    @Test
    void candidates_twoBlocks() {
        assertEquals(
            Line.bitSetList(List.of(
                List.of(1, 1, 0, 1, 0),
                List.of(1, 1, 0, 0, 1),
                List.of(0, 1, 1, 0, 1)
            )),
            Line.candidates(List.of(2, 1), 5)
        );
    }
}

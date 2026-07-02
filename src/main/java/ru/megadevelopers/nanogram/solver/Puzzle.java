package ru.megadevelopers.nanogram.solver;

import ru.megadevelopers.nanogram.model.Clue;

import java.util.List;

public record Puzzle(
        List<Clue> rowClues,
        List<Clue> columnClues,
        int width,
        int height) {
}

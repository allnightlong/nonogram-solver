package ru.megadevelopers.nanogram.solver;

import java.util.List;

public record Puzzle(
        List<List<Integer>> rowClues,
        List<List<Integer>> columnClues,
        int width,
        int height) {
}

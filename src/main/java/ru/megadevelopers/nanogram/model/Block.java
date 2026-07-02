package ru.megadevelopers.nanogram.model;

/** One block of a {@link Clue}, paired with its index among that clue's blocks. */
public record Block(int index, int length) {}

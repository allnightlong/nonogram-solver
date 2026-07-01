package ru.megadevelopers.nanogram.model;

public record BoardValueChangeEvent(int row, int column, int value) {}

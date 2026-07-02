package ru.megadevelopers.nanogram.solver;

import ru.megadevelopers.nanogram.model.Cell;

@FunctionalInterface
public interface CellListener {

    CellListener NO_OP = (row, column, value) -> {};

    void onCellChanged(int row, int column, Cell value);
}

package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;

/**
 * A live view over one row or column of a {@link Board} (or over another
 * LineView, reversed) - reads and writes go straight through to the
 * underlying cells. Never a copy.
 */
interface LineView {
    int length();

    Cell get(int index);

    void set(int index, Cell value);
}

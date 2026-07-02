package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import static ru.megadevelopers.nanogram.model.Cell.*;
import ru.megadevelopers.nanogram.solver.CellListener;

import java.util.Arrays;

/**
 * A mutable grid of cells with zero-copy row/column views - row(i)/column(i)
 * read and write straight through to the underlying array, so scanning
 * columns costs nothing extra compared to scanning rows.
 */
class Board {

    private final Cell[][] cells;

    Board(int height, int width) {
        this.cells = new Cell[height][width];
        for (Cell[] row : cells) Arrays.fill(row, NO_VALUE);
    }

    private Board(Cell[][] cells) {
        this.cells = cells;
    }

    Board copy() {
        return new Board(snapshot());
    }

    Cell[][] snapshot() {
        Cell[][] copy = new Cell[cells.length][];
        for (int row = 0; row < cells.length; row++) copy[row] = cells[row].clone();
        return copy;
    }

    int rowsCount() {
        return cells.length;
    }

    int columnCount() {
        return cells.length == 0 ? 0 : cells[0].length;
    }

    Cell get(int row, int column) {
        return cells[row][column];
    }

    void set(int row, int column, Cell value) {
        cells[row][column] = value;
    }

    LineView row(int row) {
        return new LineView() {
            public int length() {
                return cells[row].length;
            }

            public Cell get(int index) {
                return cells[row][index];
            }

            public void set(int index, Cell value) {
                cells[row][index] = value;
            }
        };
    }

    LineView column(int column) {
        return new LineView() {
            public int length() {
                return cells.length;
            }

            public Cell get(int index) {
                return cells[index][column];
            }

            public void set(int index, Cell value) {
                cells[index][column] = value;
            }
        };
    }

    boolean isFullyDetermined() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell == NO_VALUE) return false;
            }
        }
        return true;
    }

    void emitTo(CellListener listener) {
        for (int row = 0; row < cells.length; row++) {
            for (int column = 0; column < cells[row].length; column++) {
                listener.onCellChanged(row, column, cells[row][column]);
            }
        }
    }
}

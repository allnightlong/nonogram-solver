package ru.megadevelopers.nanogram.model;

import java.util.Arrays;
import java.util.List;

public class NanogramBoard {

    @FunctionalInterface
    public interface CellChangeListener {
        void onCellChanged(int row, int column, Cell value);
    }

    public final List<Clue> top;
    public final List<Clue> left;
    public final int width;
    public final int height;

    private final Cell[][] board;
    private CellChangeListener onChange;

    public NanogramBoard(List<Clue> top, List<Clue> left, int width, int height) {
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        this.board = new Cell[height][width];
        for (Cell[] row : this.board) Arrays.fill(row, Cell.NO_VALUE);
    }

    public void print(boolean printBoard) {
        printTop(leftOffset(), topOffset());
        printLeft(leftOffset(), printBoard);
        System.out.println();
    }

    public int topOffset() {
        return top.stream().mapToInt(clue -> clue.blockLengths().size()).max().orElse(0);
    }

    public int getHeightWithOffset() {
        return height + topOffset();
    }

    public int leftOffset() {
        return left.stream().mapToInt(clue -> clue.blockLengths().size()).max().orElse(0);
    }

    public int getWidthWithOffset() {
        return width + leftOffset();
    }

    public Cell getValue(int row, int column) {
        return board[row][column];
    }

    public void setValue(int row, int column, Cell value) {
        board[row][column] = value;
        if (onChange != null) onChange.onCellChanged(row, column, value);
    }

    private void printTop(int leftOffset, int topOffset) {
        for (int row = 0; row < topOffset; row++) {
            for (int i = 0; i < leftOffset; i++) System.out.print(' ');
            for (int column = 0; column < top.size(); column++) {
                List<Integer> blockLengths = top.get(column).blockLengths();
                int index = row - topOffset + blockLengths.size();
                System.out.print(index >= 0 ? toChar(blockLengths.get(index)) : ' ');
            }
            System.out.println();
        }
    }

    private void printLeft(int leftOffset, boolean printBoard) {
        for (int row = 0; row < left.size(); row++) {
            List<Integer> blockLengths = left.get(row).blockLengths();
            for (int i = 0; i < leftOffset - blockLengths.size(); i++) System.out.print(' ');
            for (int value : blockLengths) {
                System.out.print(toChar(value));
            }
            if (printBoard) {
                for (int column = 0; column < width; column++) {
                    System.out.print(getValue(row, column) == Cell.FILLED ? 'X' : '.');
                }
            }
            System.out.println();
        }
    }

    private static char toChar(int input) {
        return input != 0 ? Character.forDigit(input, 35) : ' ';
    }

    public void setOnChange(CellChangeListener onChange) {
        this.onChange = onChange;
    }
}

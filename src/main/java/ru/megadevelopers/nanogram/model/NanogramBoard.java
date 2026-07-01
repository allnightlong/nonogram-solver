package ru.megadevelopers.nanogram.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class NanogramBoard {

    @FunctionalInterface
    public interface CellChangeListener {
        void onCellChanged(int row, int column, int value);
    }

    static final int DELAY = 100;

    public final List<List<Integer>> top;
    public final List<List<Integer>> left;
    public final int width;
    public final int height;

    private final int[][] board;
    private CellChangeListener onChange;

    public NanogramBoard(List<List<Integer>> top, List<List<Integer>> left, int width, int height) {
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        this.board = new int[height][width];
    }

    public void print(boolean printBoard) {
        printTop(leftOffset(), topOffset());
        printLeft(printBoard);
        System.out.println();
    }

    public int topOffset() {
        return top.stream().mapToInt(List::size).max().orElse(0);
    }

    public int getHeightWithOffset() {
        return height + topOffset();
    }

    public int leftOffset() {
        return left.stream().mapToInt(List::size).max().orElse(0);
    }

    public int getWidthWithOffset() {
        return width + leftOffset();
    }

    public int getValue(int row, int column) {
        return board[row][column];
    }

    public void setValue(int row, int column, int value) {
        board[row][column] = value;
        if (onChange != null) onChange.onCellChanged(row, column, value);
    }

    private void printTop(int leftOffset, int topOffset) {
        for (int row = 0; row < topOffset; row++) {
            for (int i = 0; i < leftOffset; i++) System.out.print(' ');
            for (int column = 0; column < top.size(); column++) {
                System.out.print(toChar(top.get(column).get(row)));
            }
            System.out.println();
        }
    }

    private void printLeft(boolean printBoard) {
        for (int row = 0; row < left.size(); row++) {
            for (int value : left.get(row)) {
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

    public boolean solve() throws InterruptedException {
        Thread.sleep(DELAY);
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (getValue(row, column) == Cell.NO_VALUE) {
                    setValue(row, column, Cell.FILLED);
                    if (isValid(row, column) && solve()) return true;

                    setValue(row, column, Cell.EMPTY);
                    if (isValid(row, column) && solve()) return true;

                    setValue(row, column, Cell.NO_VALUE);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int row, int column) {
        return isValidRow(row) && isValidColumn(column);
    }

    private boolean isValidRow(int row) {
        List<BitSet> candidates = Line.candidates(left.get(row), width);
        List<Integer> currentLine = Arrays.stream(board[row]).boxed().toList();
        return Line.isValid(currentLine, candidates);
    }

    private boolean isValidColumn(int column) {
        List<BitSet> candidates = Line.candidates(top.get(column), height);
        List<Integer> currentLine = Arrays.stream(board).mapToInt(r -> r[column]).boxed().toList();
        return Line.isValid(currentLine, candidates);
    }

    public void setOnChange(CellChangeListener onChange) {
        this.onChange = onChange;
    }
}

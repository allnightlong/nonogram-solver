package ru.megadevelopers.nanogram.gui;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.model.NanogramBoard;
import ru.megadevelopers.nanogram.solver.v3.HybridSolver;
import ru.megadevelopers.nanogram.solver.Puzzle;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NanogramFrame extends JFrame {

    private final NanogramBoard nanogramBoard;
    private final JTextField[][] fields;

    public NanogramFrame(NanogramBoard nanogramBoard) {
        super();
        setResizable(false);
        this.nanogramBoard = nanogramBoard;
        this.fields = new JTextField[nanogramBoard.getWidthWithOffset()][nanogramBoard.getHeightWithOffset()];
        nanogramBoard.setOnChange(this::onBoardValueChange);
    }

    public void addComponentsToPane(Container pane) {
        pane.add(boardPanel(), BorderLayout.NORTH);
        pane.add(controlsPanel(), BorderLayout.SOUTH);
    }

    private JPanel boardPanel() {
        JPanel board = new JPanel();
        board.setLayout(new GridLayout(nanogramBoard.getHeightWithOffset(), nanogramBoard.getWidthWithOffset()));

        for (int row = 0; row < nanogramBoard.getHeightWithOffset(); row++) {
            for (int column = 0; column < nanogramBoard.getWidthWithOffset(); column++) {
                JTextField field = new JTextField();
                field.setEditable(false);
                board.add(field);
                fields[column][row] = field;
            }
        }

        fillFields();
        return board;
    }

    private void fillFields() {
        for (int row = 0; row < nanogramBoard.getHeightWithOffset(); row++) {
            for (int column = 0; column < nanogramBoard.getWidthWithOffset(); column++) {
                fillField(row, column);
            }
        }
    }

    private void fillField(int row, int column) {
        fields[column][row].setText(getFieldValue(row, column));
    }

    private String getFieldValue(int row, int column) {
        int topOffset = nanogramBoard.topOffset();
        int leftOffset = nanogramBoard.leftOffset();

        if (row < topOffset && column < leftOffset) return null;

        if (row >= topOffset && column >= leftOffset) {
            return nanogramBoard.getValue(row - topOffset, column - leftOffset) == Cell.FILLED ? "X" : "";
        }

        if (row < topOffset) {
            List<Integer> topColumn = nanogramBoard.top.get(column - leftOffset).stream()
                    .filter(v -> v != 0).toList();
            int index = row - topOffset + topColumn.size();
            return index >= 0 ? String.valueOf(topColumn.get(index)) : "";
        }

        List<Integer> leftRow = nanogramBoard.left.get(row - topOffset).stream()
                .filter(v -> v != 0).toList();
        int index = column - leftOffset + leftRow.size();
        return index >= 0 ? String.valueOf(leftRow.get(index)) : "";
    }

    private JPanel controlsPanel() {
        JPanel controls = new JPanel();
        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(e -> solve());
        controls.add(solveButton);
        return controls;
    }

    private void solve() {
        Puzzle puzzle = new Puzzle(nanogramBoard.left, nanogramBoard.top, nanogramBoard.width, nanogramBoard.height);
        CompletableFuture.runAsync(() -> {
            new HybridSolver().solve(puzzle, nanogramBoard::setValue);
            fillFields();
        });
    }

    private void onBoardValueChange(int row, int column, Cell value) {
        fillField(row + nanogramBoard.topOffset(), column + nanogramBoard.leftOffset());
    }
}

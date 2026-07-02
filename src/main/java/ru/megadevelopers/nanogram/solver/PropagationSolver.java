package ru.megadevelopers.nanogram.solver;

import ru.megadevelopers.nanogram.model.Cell;

/**
 * Pure constraint propagation: narrows row/column candidates against each
 * other until a fixed point. Fast, but never guesses - if propagation alone
 * can't fully determine the puzzle, reports {@link SolveResult.Ambiguous}
 * rather than picking an arbitrary candidate.
 */
public class PropagationSolver implements Solver {

    @Override
    public SolveResult solve(Puzzle puzzle, CellListener listener) {
        LineConstraintPropagator propagator = new LineConstraintPropagator(puzzle);

        if (!propagator.propagateToFixedPoint()) {
            return new SolveResult.NoSolution();
        }

        Cell[][] board = propagator.extractBoard();
        emit(board, listener);

        return propagator.isSolved() ? new SolveResult.Solved(board) : new SolveResult.Ambiguous(board);
    }

    private static void emit(Cell[][] board, CellListener listener) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (board[row][column] != Cell.NO_VALUE) {
                    listener.onCellChanged(row, column, board[row][column]);
                }
            }
        }
    }
}

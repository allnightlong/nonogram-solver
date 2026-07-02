package ru.megadevelopers.nanogram.solver.v3;

import ru.megadevelopers.nanogram.model.Cell;
import ru.megadevelopers.nanogram.solver.CellListener;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;
import ru.megadevelopers.nanogram.solver.Solver;
import ru.megadevelopers.nanogram.solver.support.LineConstraintPropagator;

/**
 * The conventional nonogram-solving algorithm: propagate row/column
 * constraints to a fixed point, and when that alone can't fully determine
 * the puzzle, guess the most constrained undetermined cell and recurse,
 * backtracking on contradiction. Complete (unlike PropagationSolver) and
 * far cheaper than naive backtracking (unlike BacktrackingSolver), since
 * candidates are pruned by propagation before any guess is made.
 */
public class HybridSolver implements Solver {

    @Override
    public SolveResult solve(Puzzle puzzle, CellListener listener) {
        SolveResult result = solve(new LineConstraintPropagator(puzzle));
        if (result instanceof SolveResult.Solved solved) {
            emit(solved.board(), listener);
        }
        return result;
    }

    private SolveResult solve(LineConstraintPropagator propagator) {
        if (!propagator.propagateToFixedPoint()) {
            return new SolveResult.NoSolution();
        }
        if (propagator.isSolved()) {
            return new SolveResult.Solved(propagator.extractBoard());
        }

        LineConstraintPropagator.GuessCell guess = propagator.findMostConstrainedCell();

        LineConstraintPropagator withFilled = propagator.copy();
        if (withFilled.restrictCell(guess, true)) {
            SolveResult result = solve(withFilled);
            if (result instanceof SolveResult.Solved) return result;
        }

        LineConstraintPropagator withEmpty = propagator.copy();
        if (withEmpty.restrictCell(guess, false)) {
            SolveResult result = solve(withEmpty);
            if (result instanceof SolveResult.Solved) return result;
        }

        return new SolveResult.NoSolution();
    }

    private static void emit(Cell[][] board, CellListener listener) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                listener.onCellChanged(row, column, board[row][column]);
            }
        }
    }
}

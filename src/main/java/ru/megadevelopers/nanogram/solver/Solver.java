package ru.megadevelopers.nanogram.solver;

public interface Solver {

    SolveResult solve(Puzzle puzzle, CellListener listener);

    default SolveResult solve(Puzzle puzzle) {
        return solve(puzzle, CellListener.NO_OP);
    }
}

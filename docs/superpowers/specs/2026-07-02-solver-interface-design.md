# Solver Interface + Three Implementations

## Context

The project has two independent, uncoordinated solving algorithms:

- **v1** (`ru.megadevelopers.nanogram.NonogramSolver`): pure constraint propagation
  over letter-encoded clue strings. Fast, but incomplete — on puzzles where
  propagation alone can't fully determine every cell, it currently prints the
  *first remaining candidate* for an ambiguous row, silently producing a wrong
  answer. Also has a hard 26-cell block-length ceiling from its `A`-`Z` clue
  encoding, and duplicates candidate-generation logic already present (and
  already more efficient) in `Line.candidates()`.
- **v2** (`ru.megadevelopers.nanogram.model.NanogramBoard.solve()`): naive
  row-major backtracking. Complete, but exponential — it regenerates each
  line's full candidate list from scratch on every validity check instead of
  computing candidates once and pruning them.

Neither uses the other's strengths. The conventional approach to nonogram
solving (see e.g. Steve Simpson's widely-cited solver writeup) is exactly
their combination: propagate constraints to a fixed point, and only fall back
to guess-and-backtrack (feeding guesses back through propagation) when logic
alone stalls. This is the "standard algorithm" and becomes v3.

This spec introduces a common `Solver` interface so v1, v2, and the new
hybrid v3 can be built, tested, and compared uniformly.

## Goals

- A single `Solver` interface implemented by three classes: `PropagationSolver`
  (v1), `BacktrackingSolver` (v2), `HybridSolver` (v3, the standard algorithm).
- Fix v1's silent-wrong-answer bug by making "solved but ambiguous" a real,
  reportable outcome instead of a guess.
- Decouple solving algorithms from `NanogramBoard` so they have no GUI
  dependency and can be unit tested directly.
- Preserve v2's live step-by-step GUI animation capability in the new shape.
- Deduplicate candidate-generation logic (`Line.candidates()` vs v1's own
  `genSequence`) and mutual-reduction logic (shared between v1 and v3).

## Non-goals

- Runtime solver selection in `StarterCli`/`StarterGui` (both starters
  hardcode `HybridSolver` this pass; v1/v2 exist as tested classes only).
- Solving multi-solution puzzles exhaustively — `HybridSolver` returns the
  first solution it finds deterministically.
- Removing or changing `Line`, `Cell`, or the JSON puzzle format.

## Design

### Core types (new package `ru.megadevelopers.nanogram.solver`)

```java
public record Puzzle(
        List<List<Integer>> rowClues,
        List<List<Integer>> columnClues,
        int width,
        int height) {}

public sealed interface SolveResult
        permits SolveResult.Solved, SolveResult.Ambiguous, SolveResult.NoSolution {
    record Solved(Cell[][] board) implements SolveResult {}
    record Ambiguous(Cell[][] partialBoard) implements SolveResult {}
    record NoSolution() implements SolveResult {}
}

@FunctionalInterface
public interface CellListener {
    void onCellChanged(int row, int column, Cell value);
    CellListener NO_OP = (row, column, value) -> {};
}

public interface Solver {
    SolveResult solve(Puzzle puzzle, CellListener listener);
    default SolveResult solve(Puzzle puzzle) {
        return solve(puzzle, CellListener.NO_OP);
    }
}
```

`SolveResult` is a tri-state outcome, not boolean success/fail. `Ambiguous`
exists specifically so `PropagationSolver` can honestly report "logic alone
couldn't fully determine this puzzle" rather than guessing — this is the fix
for v1's current bug.

`CellListener` is an algorithm-progress callback, distinct from
`NanogramBoard`'s existing `CellChangeListener` (a rendering callback). They
solve different problems and both stay.

### `PropagationSolver` (v1)

- Input: `Puzzle.rowClues` / `columnClues` directly — no letter-encoded
  parsing. (That encoding was a compact test-data notation, not part of the
  algorithm, and has a 26-cell block-length bug.)
- Uses `Line.candidates()` for candidate generation (removes the duplicate,
  less-efficient `genSequence`/`getCandidates` in old `NonogramSolver`).
- Runs mutual constraint reduction (shared `LineConstraintPropagator` helper,
  also used by `HybridSolver`) to a fixed point.
- If every line collapses to exactly one candidate → `Solved`.
- If any line's candidate list empties → `NoSolution`.
- Otherwise → `Ambiguous`, with a partial board built from cells all
  remaining candidates agree on (undetermined cells stay `NO_VALUE`).
- Never guesses — this is what makes it useful as a fast, honest baseline to
  compare against v3.

### `BacktrackingSolver` (v2)

- Extracts `NanogramBoard.solve()`/`isValid`/`isValidRow`/`isValidColumn`
  into a standalone class operating on its own internal `Cell[][]` grid (no
  `NanogramBoard` dependency).
- Row-major recursive backtracking, unchanged in spirit from today.
- Calls `listener.onCellChanged(...)` on every cell assignment, preserving
  live GUI animation.
- The existing 100ms `Thread.sleep` per step only fires when a real listener
  is attached (`listener != CellListener.NO_OP`) — it's an animation aid, not
  part of the algorithm, so it shouldn't cost anything when called headlessly
  (e.g. from tests or a future CLI `--solver=v2` path).
- Always terminates `Solved` or `NoSolution` — it's complete by construction,
  never `Ambiguous`.

### `HybridSolver` (v3 — the standard algorithm)

- Shares `LineConstraintPropagator` with `PropagationSolver`: propagate to a
  fixed point using precomputed `Line.candidates()` per row/column.
- If solved → `Solved`. If a line's candidates empty → `NoSolution`.
- If stuck but consistent (some line still has >1 candidate): pick the most
  constrained undetermined cell — the one belonging to the line (row or
  column) with the fewest remaining candidates, ties broken by row index
  then column index for determinism — guess `FILLED`, propagate again; on
  contradiction, backtrack and try `EMPTY`; if that also contradicts →
  `NoSolution`. Recurse.
- Because candidates are computed once and pruned via propagation before any
  guess, and guesses only happen on genuinely ambiguous cells (not every
  empty cell, as in naive `BacktrackingSolver`), this is expected to be
  substantially faster than v2 on non-trivial puzzles while remaining
  complete (unlike v1).

### `NanogramBoard` changes

Loses `solve()`, `isValid`, `isValidRow`, `isValidColumn`, `DELAY`. Keeps
`getValue`/`setValue`/`setOnChange`/`print` — it becomes purely a clues +
grid + rendering holder. Its own `CellChangeListener` (GUI rendering) stays
unchanged and is unrelated to the new solver-level `CellListener`.

### Package layout

```
ru.megadevelopers.nanogram.solver
    Puzzle.java
    SolveResult.java
    CellListener.java
    Solver.java
    PropagationSolver.java          (v1)
    BacktrackingSolver.java         (v2)
    HybridSolver.java               (v3)
    LineConstraintPropagator.java   (shared internal helper: reduce/reduceMutual)
```

`ru.megadevelopers.nanogram.NonogramSolver` (old v1 static-method class with
letter-encoded sample puzzles) is deleted; its role is fully replaced by
`PropagationSolver`. Sample puzzle data used in tests is rewritten as plain
`List.of(...)` clue literals, matching the style already used in `LineTest`.

### Starters

`StarterCli`/`StarterGui` build a `Puzzle` from the parsed JSON (instead of
constructing `NanogramBoard` and calling `board.solve()`), instantiate
`new HybridSolver()`, and call `solver.solve(puzzle, listener)`:

- CLI: no listener needed — `solver.solve(puzzle)` (default, synchronous),
  then print the resulting board.
- GUI: `solver.solve(puzzle, (r, c, v) -> board.setValue(r, c, v))` inside
  the existing `CompletableFuture.runAsync`, so animation flows through
  `NanogramBoard`'s existing render-notification path unchanged.

### Testing

- `PropagationSolverTest`, `BacktrackingSolverTest`, `HybridSolverTest`: each
  covers a solvable puzzle → `Solved`, an unsatisfiable clue set →
  `NoSolution`, and (for `PropagationSolver` only) a puzzle that requires
  guessing → `Ambiguous`.
- A shared parameterized test runs the same set of solvable puzzles through
  all three `Solver` implementations and asserts they all report `Solved`
  with an identical board — a regression guard that all three agree.
- `LineTest` is unchanged (`Line` itself doesn't change).

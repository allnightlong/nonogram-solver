# v4: DP-Based Line Solver, and Splitting Solvers Into Per-Version Packages

## Context

The `ru.megadevelopers.nanogram.solver` package currently holds three `Solver`
implementations (`PropagationSolver` v1, `BacktrackingSolver` v2,
`HybridSolver` v3) flat in one package, plus their shared data model
(`Puzzle`, `SolveResult`, `CellListener`, `Solver`) and one shared internal
helper (`LineConstraintPropagator`, used by v1 and v3).

v1 and v3 both determine forced cells the same way: generate every valid
placement of a line's clue as a `BitSet` (`Line.candidates()`), then
intersect those candidates across rows/columns to find cells every
candidate agrees on. This is correct, but its cost is bounded by the number
of valid placements, which is combinatorial (`C(slack + blocks, blocks)`)
and can blow up badly for long lines with many small blocks - independent of
the recent optimization that made generating each individual candidate
cheaper (that reduced constant-factor cost per candidate, not the count).

Steve Simpson's canonical nonogram line-solving technique avoids this
entirely: two DP passes (leftmost-fit, rightmost-fit) determine forced
cells in `O(N x K)` (line length x block count), regardless of how many
placements exist in principle. This is a structurally different, and for
worst-case lines, significantly more efficient technique. This spec adds a
fourth solver (`DpSolver`, v4) built on it, and reorganizes the `solver`
package so the four implementations' relative order is unmistakable from
the package structure alone.

## Goals

- Add `DpSolver` (v4): a fully independent implementation (no shared code
  with v1/v2/v3) using DP-based per-line forced-cell computation instead of
  candidate enumeration, wrapped in the same propagate-to-fixed-point,
  guess-most-constrained-cell, backtrack-on-contradiction shape as v3 (for
  a meaningful comparison), but implemented from scratch.
- Split `PropagationSolver`, `BacktrackingSolver`, `HybridSolver`, and the
  new `DpSolver` into `solver.v1` / `solver.v2` / `solver.v3` / `solver.v4`
  respectively, so package structure alone shows the intended order.
- Keep version-agnostic shared types (`Puzzle`, `SolveResult`,
  `CellListener`, `Solver`) in the top-level `solver` package.
- Move `LineConstraintPropagator` (shared by v1 and v3) to
  `solver.support`, made `public` since package-private access no longer
  reaches across the new `v1`/`v3` package boundary.

## Non-goals

- No timing/benchmark test asserting v4 is faster than v3 - wall-clock
  assertions are flaky. The efficiency argument rests on the structural
  design (DP never enumerates candidates); correctness is verified
  functionally, same as the other three solvers.
- No change to `Line`, `Cell`, or the JSON puzzle format.
- `StarterCli`/`StarterGui` continue to hardcode v3 (`HybridSolver`) per the
  earlier design decision - v4 is not wired into either starter this pass,
  same as v1/v2 today.

## Design

### Package layout

```
ru.megadevelopers.nanogram.solver
    Puzzle.java
    SolveResult.java
    CellListener.java
    Solver.java
    support/
        LineConstraintPropagator.java   (public; shared by v1 and v3)
    v1/
        PropagationSolver.java
    v2/
        BacktrackingSolver.java
    v3/
        HybridSolver.java
    v4/
        DpSolver.java
        LineDpSolver.java               (package-private, v4-internal only)
```

`Line` (in `ru.megadevelopers.nanogram.model`) is untouched - v1/v3 keep
consuming `Line.candidates()` via `LineConstraintPropagator`; v4 does not
use `Line.candidates()` at all, since its whole point is avoiding candidate
enumeration.

Tests mirror the same split: `solver.v1.PropagationSolverTest`,
`solver.v2.BacktrackingSolverTest`, `solver.v3.HybridSolverTest`,
`solver.v4.DpSolverTest`. `SolverAgreementTest` stays at the top level
(`ru.megadevelopers.nanogram.solver`) since it exercises all four.

`StarterCli`, `StarterGui`, and `NanogramFrame` update their
`ru.megadevelopers.nanogram.solver.HybridSolver` import to
`ru.megadevelopers.nanogram.solver.v3.HybridSolver`.

### `LineConstraintPropagator` visibility change

Moving to `solver.support` while still being called from `solver.v1` and
`solver.v3` means Java's package-private default no longer works - the
class itself, its constructor, and every member currently used externally
(`propagateToFixedPoint`, `isSolved`, `extractBoard`, `copy`,
`findMostConstrainedCell`, `restrictCell`, and the nested `GuessCell`
record) become `public`. Internal-only members (`reduceMutual`, `reduce`,
`cellAt`, `firstUndetermined`, `allAgree`, `deepCopy`, the private copy
constructor) are unaffected.

### `DpSolver` (v4): algorithm

**Board representation:** a plain `Cell[][]` grid - no candidate lists, no
`BitSet`s anywhere in v4.

**Per-line DP (`LineDpSolver`, v4-internal):** given a line's clue and its
*current* cells (some already `FILLED`/`EMPTY` from cross-line propagation
or a guess, the rest `NO_VALUE`), compute each block's leftmost-feasible
and rightmost-feasible start position via two linear DP passes that respect
the already-known cells (a block's placement must not land on a known-EMPTY
cell it would otherwise cover, and must cover any known-FILLED cell that
only it could plausibly cover). A block's *guaranteed-overlap* region -
where its leftmost and rightmost feasible windows overlap - is forced
`FILLED`. A cell not reachable by any block's full feasible window (across
all blocks) is forced `EMPTY`. If any block has no feasible placement at
all given the current known cells, the line - and therefore the whole
board state - is infeasible (this is the contradiction signal).

**Propagation loop:** repeatedly run `LineDpSolver` over every row, then
every column, applying newly-forced cells to the grid, until a full
row+column pass makes no changes (fixed point) or some line is found
infeasible (contradiction -> `NoSolution` for this branch).

**Guessing:** if propagation stabilizes without fully determining the
board, pick the row or column with the *fewest remaining undetermined
cells* (the DP-world analogue of v3's "fewest candidates" MRV heuristic;
same tie-break: prefer rows, then lowest index), guess `FILLED` on its
first undetermined cell, and recurse through the whole propagate step
again from that assumption. On contradiction, backtrack and try `EMPTY`. If
both fail, this branch is genuinely unsolvable.

This mirrors v3's propagate/check/guess/backtrack shape conceptually (so
comparing the two is meaningful) but shares no code with it - v4 is a
ground-up implementation, per the decision that v4 should be "fully
independent."

**Result reporting:** identical contract to the other three - `DpSolver`
implements `Solver`, returns `SolveResult.Solved`/`NoSolution` (never
`Ambiguous` - like `BacktrackingSolver` and `HybridSolver`, it's complete by
construction since it backtracks), and only calls the `CellListener` once,
over the finished board, when a `Solved` result is produced (no per-step
animation, matching v3's behavior).

### Testing

- `DpSolverTest`: reuses the same hand-verified fixtures already used for
  v1/v2/v3 - the all-filled 3x3 (trivial), the plus-sign 3x3 (needs cross-
  line propagation but no guessing), the impossible 1x1 (`NoSolution`), and
  the all-`[1]` permutation 3x3 (provably requires guessing by the same
  symmetry argument used for `PropagationSolverTest`/`HybridSolverTest` -
  DP propagation makes zero progress on it for the identical reason: every
  row/column's leftmost and rightmost windows span the entire line with no
  forced overlap, since any single cell could be the `[1]` block).
- `SolverAgreementTest`: extended to include `DpSolver` in the parameterized
  "all solvers agree on uniquely-solvable puzzles" check, giving strong
  regression confidence that an independently-implemented algorithm
  produces results identical to the three already-verified solvers.

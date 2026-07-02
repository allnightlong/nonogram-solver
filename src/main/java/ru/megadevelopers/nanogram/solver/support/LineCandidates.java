package ru.megadevelopers.nanogram.solver.support;

import ru.megadevelopers.nanogram.model.Cell;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * The remaining candidate placements for one line (row or column) - each
 * BitSet is one full placement of that line's clue still consistent with
 * every other line's candidates so far. Narrows via removeIf as
 * propagation rules placements out; once only one remains, the line is
 * fully determined.
 */
class LineCandidates implements Iterable<BitSet> {

    private final List<BitSet> candidates;

    LineCandidates(List<BitSet> candidates) {
        this.candidates = new ArrayList<>(candidates);
    }

    int size() {
        return candidates.size();
    }

    boolean isEmpty() {
        return candidates.isEmpty();
    }

    boolean removeIf(Predicate<BitSet> predicate) {
        return candidates.removeIf(predicate);
    }

    /** The forced value at this position if every remaining candidate agrees, else NO_VALUE. */
    Cell cellAt(int index) {
        if (!allAgree(index)) return Cell.NO_VALUE;
        return candidates.get(0).get(index) ? Cell.FILLED : Cell.EMPTY;
    }

    /** The first position where remaining candidates disagree. */
    int firstUndetermined(int bound) {
        for (int i = 0; i < bound; i++) {
            if (!allAgree(i)) return i;
        }
        throw new IllegalStateException("line has no undetermined cell");
    }

    private boolean allAgree(int index) {
        boolean first = candidates.get(0).get(index);
        for (BitSet candidate : candidates) {
            if (candidate.get(index) != first) return false;
        }
        return true;
    }

    LineCandidates copy() {
        List<BitSet> cloned = new ArrayList<>();
        for (BitSet candidate : candidates) cloned.add((BitSet) candidate.clone());
        return new LineCandidates(cloned);
    }

    @Override
    public Iterator<BitSet> iterator() {
        return candidates.iterator();
    }
}

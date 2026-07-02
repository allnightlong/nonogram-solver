package ru.megadevelopers.nanogram.solver.v4;

import ru.megadevelopers.nanogram.model.Cell;
import static ru.megadevelopers.nanogram.model.Cell.*;

/**
 * A live view over one row or column of a {@link Board} (or over another
 * LineView, reversed) - reads and writes go straight through to the
 * underlying cells. Never a copy.
 */
interface LineView {
    int length();

    Cell get(int index);

    void set(int index, Cell value);

    /** A view of this line with positions read/written back to front. */
    default LineView reversed() {
        LineView self = this;
        return new LineView() {
            public int length() {
                return self.length();
            }

            public Cell get(int index) {
                return self.get(self.length() - 1 - index);
            }

            public void set(int index, Cell value) {
                self.set(self.length() - 1 - index, value);
            }
        };
    }

    default int countUndetermined() {
        int count = 0;
        for (int i = 0; i < length(); i++) {
            if (get(i) == NO_VALUE) count++;
        }
        return count;
    }

    default int firstUndetermined() {
        for (int i = 0; i < length(); i++) {
            if (get(i) == NO_VALUE) return i;
        }
        throw new IllegalStateException("line has no undetermined cell");
    }

    /** Writes each non-NO_VALUE entry of {@code forced} into the corresponding still-undetermined cell. Returns whether anything changed. */
    default boolean applyForced(Cell[] forced) {
        boolean changed = false;
        for (int i = 0; i < forced.length; i++) {
            if (forced[i] != NO_VALUE && get(i) == NO_VALUE) {
                set(i, forced[i]);
                changed = true;
            }
        }
        return changed;
    }
}

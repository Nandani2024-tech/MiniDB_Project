package com.minidb.storage;

import java.util.List;

/**
 * Represents a row of typed column values.
 */
public class Row {
    private final Object[] values;

    public Row(Object[] values) {
        this.values = values;
        // TODO: Phase 1 - Implement fixed-width binary serialization.
    }

    public Row(List<Object> values) {
        this.values = values.toArray();
        // TODO: Phase 1 - Implement fixed-width binary serialization.
    }

    // TODO: Add methods to access column values.
}

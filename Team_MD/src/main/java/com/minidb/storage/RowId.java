package com.minidb.storage;

import java.util.Objects;

/**
 * A simple immutable identifier for a row, consisting of a page ID and a slot number.
 */
public record RowId(int pageId, int slotNumber) {
    // Records automatically handle equals, hashCode, and toString.
}

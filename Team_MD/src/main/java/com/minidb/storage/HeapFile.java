package com.minidb.storage;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The table abstraction representing a collection of pages stored in a file.
 */
public class HeapFile {
    private final BufferPool bufferPool;
    private final PageManager pageManager;
    private final List<ColumnType> schema;

    public HeapFile(BufferPool bufferPool, PageManager pageManager, List<ColumnType> schema) {
        this.bufferPool = bufferPool;
        this.pageManager = pageManager;
        this.schema = schema;
    }

    /**
     * Inserts a row into the table.
     * Searches for a page with enough space or allocates a new one.
     */
    public RowId insert(Row row) throws IOException {
        byte[] rowBytes = Row.serialize(row, schema);
        int numPages = pageManager.getNumPages();
        
        Page targetPage = null;
        int slotNumber = -1;

        // Scan existing pages for space
        for (int i = 0; i < numPages; i++) {
            Page page = bufferPool.getPage(i);
            slotNumber = page.insertRow(rowBytes);
            if (slotNumber != -1) {
                targetPage = page;
                break;
            }
        }

        // No space in existing pages, allocate new one
        if (targetPage == null) {
            targetPage = pageManager.allocatePage();
            bufferPool.addPage(targetPage);
            slotNumber = targetPage.insertRow(rowBytes);
        }

        bufferPool.markDirty(targetPage.getPageId());
        return new RowId(targetPage.getPageId(), slotNumber);
    }

    public Row get(RowId rowId) throws IOException {
        Page page = bufferPool.getPage(rowId.pageId());
        byte[] bytes = page.getRow(rowId.slotNumber());
        if (bytes == null) return null;
        return Row.deserialize(bytes, schema);
    }

    public void delete(RowId rowId) throws IOException {
        Page page = bufferPool.getPage(rowId.pageId());
        page.deleteRow(rowId.slotNumber());
        bufferPool.markDirty(rowId.pageId());
    }

    public List<Row> scan() throws IOException {
        List<Row> results = new ArrayList<>();
        int numPages = pageManager.getNumPages();
        for (int i = 0; i < numPages; i++) {
            Page page = bufferPool.getPage(i);
            for (int s = 0; s < page.getSlotCount(); s++) {
                byte[] bytes = page.getRow(s);
                if (bytes != null) {
                    results.add(Row.deserialize(bytes, schema));
                }
            }
        }
        return results;
    }

    public List<Map.Entry<RowId, Row>> scanWithIds() throws IOException {
        List<Map.Entry<RowId, Row>> results = new ArrayList<>();
        int numPages = pageManager.getNumPages();
        for (int i = 0; i < numPages; i++) {
            Page page = bufferPool.getPage(i);
            for (int s = 0; s < page.getSlotCount(); s++) {
                byte[] bytes = page.getRow(s);
                if (bytes != null) {
                    RowId rid = new RowId(i, s);
                    Row row = Row.deserialize(bytes, schema);
                    results.add(new AbstractMap.SimpleEntry<>(rid, row));
                }
            }
        }
        return results;
    }
}

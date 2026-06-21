package com.minidb.index;

import com.minidb.storage.PageManager;
import com.minidb.storage.RowId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BTreeIndexTest {
    private static final String INDEX_FILE = "test_index.db";

    @BeforeEach
    public void setup() {
        new File(INDEX_FILE).delete();
    }

    @AfterEach
    public void teardown() {
        new File(INDEX_FILE).delete();
    }

    @Test
    public void testEmptyIndexInitialization() throws IOException {
        BTreeIndex index = new BTreeIndex(INDEX_FILE);
        try {
            List<RowId> results = index.search(10);
            assertTrue(results.isEmpty());
        } finally {
            index.close();
        }
    }

    @Test
    public void testInsertAndSearch() throws IOException {
        BTreeIndex index = new BTreeIndex(INDEX_FILE);
        try {
            for (int i = 0; i < 100; i++) {
                RowId rowId = new RowId(1, i);
                index.insert(i, rowId);
            }
            
            List<RowId> result = index.search(50);
            assertEquals(1, result.size());
            assertEquals(50, result.get(0).slotNumber());
        } finally {
            index.close();
        }
    }

    @Test
    public void testNodeSplitting() throws IOException {
        BTreeIndex index = new BTreeIndex(INDEX_FILE);
        try {
            for (int i = 0; i < 50; i++) {
                index.insert(i, new RowId(1, i));
            }
            
            for (int i = 0; i < 50; i++) {
                List<RowId> result = index.search(i);
                assertEquals(1, result.size());
                assertEquals(i, result.get(0).slotNumber());
            }
        } finally {
            index.close();
        }
    }

    @Test
    public void testPersistence() throws IOException {
        BTreeIndex index1 = new BTreeIndex(INDEX_FILE);
        try {
            for (int i = 0; i < 30; i++) {
                index1.insert(i, new RowId(2, i));
            }
        } finally {
            index1.close();
        }
        
        BTreeIndex index2 = new BTreeIndex(INDEX_FILE);
        try {
            List<RowId> result = index2.search(15);
            assertEquals(1, result.size());
            assertEquals(15, result.get(0).slotNumber());
        } finally {
            index2.close();
        }
    }
}

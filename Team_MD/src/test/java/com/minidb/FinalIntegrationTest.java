package com.minidb;

import com.minidb.index.BTreeIndex;
import com.minidb.query.Operator;
import com.minidb.query.QueryOptimizer;
import com.minidb.query.operators.*;
import com.minidb.storage.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FinalIntegrationTest {

    private static final String TABLE_FILE = "integration_table.db";
    private static final String INDEX_FILE = "integration_index.db";

    private PageManager pageManager;
    private HeapFile heapFile;
    private List<ColumnType> schema;

    @BeforeEach
    public void setup() throws Exception {
        new File(TABLE_FILE).delete();
        new File(INDEX_FILE).delete();
        schema = Arrays.asList(ColumnType.INT, ColumnType.VARCHAR);
        pageManager = new PageManager(TABLE_FILE);
        heapFile = new HeapFile(new BufferPool(pageManager, 32), pageManager, schema);
    }

    @AfterEach
    public void teardown() throws Exception {
        pageManager.close();
        new File(TABLE_FILE).delete();
        new File(INDEX_FILE).delete();
    }

    @Test
    public void test1_BasicStorageAndQuery() throws Exception {
        System.out.println("Running Test 1: Basic Storage & Query...");
        // 1. Insert records
        heapFile.insert(new Row(new Object[]{1, "Alice"}));
        heapFile.insert(new Row(new Object[]{2, "Bob"}));

        // 2. Query (SeqScan)
        SeqScanOperator scan = new SeqScanOperator(heapFile);
        
        // 3. Filter (WHERE id = 2)
        FilterOperator.Predicate pred = row -> (int) row.getValue(0) == 2;
        FilterOperator filter = new FilterOperator(scan, pred);

        filter.open();
        Row result = filter.next();
        assertNotNull(result, "Row should be found");
        assertEquals("Bob", result.getValue(1), "Expected Bob");
        assertNull(filter.next(), "Only one row should be returned");
        filter.close();
        System.out.println("Test 1 Passed!");
    }

    @Test
    public void test2_BPlusTreeAndOptimizer() throws Exception {
        System.out.println("Running Test 2: B+ Tree & Optimizer...");
        BTreeIndex index = new BTreeIndex(INDEX_FILE);
        
        try {
            // Insert 100 records
            for (int i = 0; i < 100; i++) {
                RowId rowId = heapFile.insert(new Row(new Object[]{i, "User" + i}));
                index.insert(i, rowId);
            }

            QueryOptimizer optimizer = new QueryOptimizer();
            
            // Query 1: Unindexed column (simulate age by not passing index)
            FilterOperator.Predicate pred = row -> (int) row.getValue(0) == 50;
            Operator op1 = optimizer.optimize("SELECT * FROM users WHERE id = 50",
                heapFile, null, pred, -1, null);
            assertTrue(op1 instanceof FilterOperator, "Should use SeqScan + Filter when no index provided");

            // Query 2: Indexed column
            Operator op2 = optimizer.optimize("SELECT * FROM users WHERE id = 50",
                heapFile, index, null, 50, null);
            assertTrue(op2 instanceof IndexScanOperator, "Should use IndexScan when index is available");
            
            op2.open();
            Row result = op2.next();
            assertNotNull(result);
            assertEquals("User50", result.getValue(1));
            op2.close();
            System.out.println("Test 2 Passed!");
        } finally {
            index.close();
        }
    }

    @Test
    public void test3_MVCCConcurrency() {
        System.out.println("Running Test 3: MVCC Concurrency...");
        fail("MVCC Concurrency test failed: Transaction Manager and MVCC logic are NOT IMPLEMENTED in the codebase.");
    }

    @Test
    public void test4_CrashRecovery() {
        System.out.println("Running Test 4: Crash Recovery...");
        fail("Crash Recovery test failed: Write-Ahead Log (WAL) and Recovery logic are NOT IMPLEMENTED in the codebase.");
    }
}

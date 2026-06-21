package com.minidb.query.operators;

import com.minidb.query.Operator;
import com.minidb.storage.HeapFile;
import com.minidb.storage.Row;

import java.util.Iterator;
import java.util.List;

public class InsertOperator implements Operator {
    private HeapFile heapFile;
    private Iterator<Row> rowsToInsert;
    private int insertCount = 0;
    private boolean executed = false;

    public InsertOperator(HeapFile heapFile, List<Row> rowsToInsert) {
        this.heapFile = heapFile;
        this.rowsToInsert = rowsToInsert.iterator();
    }

    @Override
    public void open() throws Exception {
        insertCount = 0;
        executed = false;
    }

    @Override
    public Row next() throws Exception {
        if (executed) return null;

        while (rowsToInsert.hasNext()) {
            heapFile.insert(rowsToInsert.next());
            insertCount++;
        }
        executed = true;
        return new Row(new Object[]{insertCount});
    }

    @Override
    public void close() throws Exception {
    }
}

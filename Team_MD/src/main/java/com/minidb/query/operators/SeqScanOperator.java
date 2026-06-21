package com.minidb.query.operators;

import com.minidb.query.Operator;
import com.minidb.storage.HeapFile;
import com.minidb.storage.Row;

import java.util.Iterator;
import java.util.List;

public class SeqScanOperator implements Operator {
    private HeapFile heapFile;
    private Iterator<Row> rowIterator;

    public SeqScanOperator(HeapFile heapFile) {
        this.heapFile = heapFile;
    }

    @Override
    public void open() throws Exception {
        List<Row> rows = heapFile.scan();
        rowIterator = rows.iterator();
    }

    @Override
    public Row next() throws Exception {
        if (rowIterator != null && rowIterator.hasNext()) {
            return rowIterator.next();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        rowIterator = null;
    }
}

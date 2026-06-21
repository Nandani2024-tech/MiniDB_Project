package com.minidb.query.operators;

import com.minidb.index.BTreeIndex;
import com.minidb.query.Operator;
import com.minidb.storage.HeapFile;
import com.minidb.storage.Row;
import com.minidb.storage.RowId;

import java.util.Iterator;
import java.util.List;

public class IndexScanOperator implements Operator {
    private BTreeIndex index;
    private HeapFile heapFile;
    private int searchKey;
    private Iterator<RowId> resultIterator;

    public IndexScanOperator(BTreeIndex index, HeapFile heapFile, int searchKey) {
        this.index = index;
        this.heapFile = heapFile;
        this.searchKey = searchKey;
    }

    @Override
    public void open() throws Exception {
        List<RowId> results = index.search(searchKey);
        resultIterator = results.iterator();
    }

    @Override
    public Row next() throws Exception {
        if (resultIterator != null && resultIterator.hasNext()) {
            RowId rid = resultIterator.next();
            return heapFile.get(rid);
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        resultIterator = null;
    }
}

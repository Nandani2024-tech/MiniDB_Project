package com.minidb.query;

import com.minidb.index.BTreeIndex;
import com.minidb.query.operators.*;
import com.minidb.storage.HeapFile;
import com.minidb.storage.Row;

import java.util.List;
import java.util.Map;

public class QueryOptimizer {

    public Operator optimize(String sql, HeapFile heapFile, BTreeIndex index, FilterOperator.Predicate predicate, int searchKey, List<Integer> projections) {
        // Simplified optimizer for testing
        Operator op;
        if (index != null && searchKey != -1) {
            op = new IndexScanOperator(index, heapFile, searchKey);
        } else {
            op = new SeqScanOperator(heapFile);
            if (predicate != null) {
                op = new FilterOperator(op, predicate);
            }
        }
        
        if (projections != null && !projections.isEmpty()) {
            op = new ProjectOperator(op, projections);
        }
        
        return op;
    }
}

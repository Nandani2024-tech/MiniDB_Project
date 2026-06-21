package com.minidb.index;

import com.minidb.storage.Page;
import com.minidb.storage.PageManager;
import com.minidb.storage.RowId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BTreeIndex {
    private PageManager pageManager;
    private IndexMetadata metadata;
    private String indexFileName;

    public BTreeIndex(String indexFileName) {
        this.indexFileName = indexFileName;
        try {
            this.pageManager = new PageManager(indexFileName);
            loadOrInitializeMetadata();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize BTreeIndex", e);
        }
    }

    private void loadOrInitializeMetadata() throws IOException {
        if (pageManager.getNumPages() == 0) {
            metadata = new IndexMetadata();
            pageManager.allocatePage(); // Allocates page 0 (metadata)
            int rootNodeId = allocateNewNode(true).getNodeId(); // Allocates page 1
            metadata.setRootNodeId(rootNodeId);
            saveMetadata();
        } else {
            Page metaPage = pageManager.readPage(0);
            byte[] data = new byte[4096 - Page.HEADER_SIZE];
            System.arraycopy(metaPage.getRawBytes(), Page.HEADER_SIZE, data, 0, data.length);
            metadata = IndexMetadata.deserialize(data);
        }
    }

    private void saveMetadata() throws IOException {
        Page metaPage = pageManager.readPage(0);
        byte[] serializedMeta = metadata.serialize();
        System.arraycopy(serializedMeta, 0, metaPage.getRawBytes(), Page.HEADER_SIZE, serializedMeta.length);
        pageManager.writePage(metaPage);
    }

    private BTreeNode allocateNewNode(boolean isLeaf) throws IOException {
        Page page = pageManager.allocatePage();
        BTreeNode node = new BTreeNode(page.getPageId(), isLeaf);
        saveNode(node);
        metadata.incrementNumNodes();
        saveMetadata();
        return node;
    }

    private BTreeNode loadNode(int nodeId) {
        try {
            Page page = pageManager.readPage(nodeId);
            byte[] data = new byte[4096 - Page.HEADER_SIZE];
            System.arraycopy(page.getRawBytes(), Page.HEADER_SIZE, data, 0, data.length);
            return BTreeNode.deserialize(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load node " + nodeId, e);
        }
    }

    private void saveNode(BTreeNode node) {
        try {
            Page page = pageManager.readPage(node.getNodeId());
            byte[] serializedNode = node.serialize();
            System.arraycopy(serializedNode, 0, page.getRawBytes(), Page.HEADER_SIZE, serializedNode.length);
            pageManager.writePage(page);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save node " + node.getNodeId(), e);
        }
    }

    public List<RowId> search(int key) {
        if (metadata.getRootNodeId() == -1) return new ArrayList<>();
        return findLeafNode(key).search(key);
    }

    private BTreeNode findLeafNode(int key) {
        BTreeNode current = loadNode(metadata.getRootNodeId());
        while (!current.isLeaf()) {
            int idx = current.findKeyIndex(key);
            if (idx < current.getNumKeys() && current.getKeys().get(idx) == key) idx++;
            current = loadNode(current.getChildPointers().get(idx));
        }
        return current;
    }

    public void insert(int key, RowId rowId) {
        try {
            BTreeNode root = loadNode(metadata.getRootNodeId());
            if (root.isFull()) {
                BTreeNode newRoot = allocateNewNode(false);
                newRoot.getChildPointers().add(root.getNodeId());
                splitChild(newRoot, 0, root);
                metadata.setRootNodeId(newRoot.getNodeId());
                saveMetadata();
                insertNonFull(newRoot, key, rowId);
            } else {
                insertNonFull(root, key, rowId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to insert", e);
        }
    }

    private void insertNonFull(BTreeNode node, int key, RowId rowId) throws IOException {
        if (node.isLeaf()) {
            node.insertLeaf(key, rowId);
            saveNode(node);
        } else {
            int idx = node.findKeyIndex(key);
            if (idx < node.getNumKeys() && node.getKeys().get(idx) == key) idx++;
            
            BTreeNode child = loadNode(node.getChildPointers().get(idx));
            if (child.isFull()) {
                splitChild(node, idx, child);
                if (key >= node.getKeys().get(idx)) idx++;
                child = loadNode(node.getChildPointers().get(idx));
            }
            insertNonFull(child, key, rowId);
        }
    }

    private void splitChild(BTreeNode parent, int childIndex, BTreeNode child) throws IOException {
        BTreeNode rightNode = allocateNewNode(child.isLeaf());
        int midKey = child.getKeys().get(child.getNumKeys() / 2); // For internal nodes, this key moves up
        
        BTreeNode newRight = child.split(rightNode.getNodeId()); // splits child in place
        
        // Copy the split contents to rightNode
        rightNode = newRight;
        saveNode(child);
        saveNode(rightNode);
        
        // Internal nodes promote the midKey, leaf nodes promote midKey (copy)
        int promoteKey = child.isLeaf() ? rightNode.getKeys().get(0) : midKey;
        
        parent.insertInternal(promoteKey, rightNode.getNodeId());
        saveNode(parent);
    }

    public void delete(int key) {
        // Deletion omitted for simplicity in Phase 2
    }

    public void close() throws IOException {
        pageManager.close();
    }
}

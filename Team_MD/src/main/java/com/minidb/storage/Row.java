package com.minidb.storage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Represents a row of typed column values.
 */
public class Row {
    private final Object[] values;

    public Row(Object[] values) {
        this.values = values;
    }

    public Row(List<Object> values) {
        this.values = values.toArray();
    }

    public Object[] getValues() {
        return values;
    }

    public Object getValue(int index) {
        return values[index];
    }

    /**
     * Serializes a row's typed values into a byte array.
     * INT: 4 bytes
     * VARCHAR: 4-byte length + UTF-8 bytes
     */
    public static byte[] serialize(Row row, List<ColumnType> schema) {
        int estimatedSize = 0;
        for (int i = 0; i < schema.size(); i++) {
            if (schema.get(i) == ColumnType.INT) {
                estimatedSize += 4;
            } else if (schema.get(i) == ColumnType.VARCHAR) {
                String s = (String) row.getValue(i);
                estimatedSize += 4 + s.getBytes(StandardCharsets.UTF_8).length;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(estimatedSize);
        for (int i = 0; i < schema.size(); i++) {
            if (schema.get(i) == ColumnType.INT) {
                buffer.putInt((Integer) row.getValue(i));
            } else if (schema.get(i) == ColumnType.VARCHAR) {
                byte[] stringBytes = ((String) row.getValue(i)).getBytes(StandardCharsets.UTF_8);
                buffer.putInt(stringBytes.length);
                buffer.put(stringBytes);
            }
        }
        return buffer.array();
    }

    /**
     * Deserializes a byte array into a Row object based on the schema.
     */
    public static Row deserialize(byte[] bytes, List<ColumnType> schema) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Object[] values = new Object[schema.size()];
        for (int i = 0; i < schema.size(); i++) {
            if (schema.get(i) == ColumnType.INT) {
                values[i] = buffer.getInt();
            } else if (schema.get(i) == ColumnType.VARCHAR) {
                int length = buffer.getInt();
                byte[] stringBytes = new byte[length];
                buffer.get(stringBytes);
                values[i] = new String(stringBytes, StandardCharsets.UTF_8);
            }
        }
        return new Row(values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Row{");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        if (values.length != row.values.length) return false;
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals(row.values[i])) return false;
        }
        return true;
    }
}

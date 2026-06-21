package com.minidb;

import com.minidb.query.operators.SeqScanOperator;
import com.minidb.storage.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {
    private static final String DB_FILE = "minidb.db";
    private PageManager pageManager;
    private BufferPool bufferPool;
    private HeapFile heapFile;
    private List<ColumnType> defaultSchema;

    public void start() {
        printHeader();

        try {
            // Setup Default Schema (ID: INT, Name: VARCHAR)
            defaultSchema = new ArrayList<>();
            defaultSchema.add(ColumnType.INT);
            defaultSchema.add(ColumnType.VARCHAR);

            // Initialize Storage Stack
            pageManager = new PageManager(DB_FILE);
            bufferPool = new BufferPool(pageManager, 50);
            heapFile = new HeapFile(bufferPool, pageManager, defaultSchema);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("minidb> ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase(".exit")) break;
                if (input.equalsIgnoreCase(".status")) {
                    showStatus();
                    continue;
                }
                if (input.equalsIgnoreCase(".help")) {
                    showHelp();
                    continue;
                }
                if (input.isEmpty()) continue;

                processQuery(input);
            }

            // Cleanup
            bufferPool.flushAll();
            pageManager.close();
            System.out.println("\n[System] All pages flushed. Storage safely closed.");
            System.out.println("Goodbye!");

        } catch (Exception e) {
            System.err.println("Critical System Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processQuery(String sql) {
        try {
            if (!sql.endsWith(";")) sql += ";";
            Statement statement = CCJSqlParserUtil.parse(sql);

            if (statement instanceof Select) {
                executeSelect((Select) statement);
            } else if (statement instanceof Insert) {
                executeInsert((Insert) statement);
            } else {
                System.out.println("[Error] Command '" + statement.getClass().getSimpleName() + "' parsed but not yet implemented in Shell.");
            }
        } catch (Exception e) {
            System.out.println("[SQL Error] " + e.getMessage());
        }
    }

    private void executeSelect(Select select) throws Exception {
        SeqScanOperator scan = new SeqScanOperator(heapFile);
        scan.open();
        
        System.out.println("+------------+-------------------------+");
        System.out.println("| ID (INT)   | NAME (VARCHAR)          |");
        System.out.println("+------------+-------------------------+");
        
        Row row;
        int count = 0;
        while ((row = scan.next()) != null) {
            Object id = row.getValues()[0];
            Object name = row.getValues()[1];
            System.out.printf("| %-10s | %-23s |\n", id, name);
            count++;
        }
        
        System.out.println("+------------+-------------------------+");
        System.out.println(count + " rows in set.");
        scan.close();
    }

    private void executeInsert(Insert insert) throws Exception {
        if (insert.getValues() != null) {
            List<Expression> expressions = insert.getValues().getExpressions();
            
            Object[] values = new Object[2];
            if (expressions.size() >= 1 && expressions.get(0) instanceof LongValue) {
                values[0] = (int) ((LongValue) expressions.get(0)).getValue();
            }
            if (expressions.size() >= 2 && expressions.get(1) instanceof StringValue) {
                values[1] = ((StringValue) expressions.get(1)).getValue();
            }

            Row row = new Row(values);
            RowId rid = heapFile.insert(row);
            System.out.println("Success: Inserted at " + rid);
        } else {
            System.out.println("[Error] Insert format not supported. Use: INSERT INTO users VALUES (1, 'name');");
        }
    }

    private void showStatus() throws IOException {
        System.out.println("\n--- System Status ---");
        System.out.println("Persistent File : " + DB_FILE);
        System.out.println("Disk Utilization: " + pageManager.getNumPages() + " pages (" + (pageManager.getNumPages() * 4) + " KB)");
        System.out.println("BufferPool      : " + bufferPool.getLoadedPageCount() + " / 50 pages loaded");
        System.out.println("Schema (Default): users [id INT, name VARCHAR]");
        System.out.println("---------------------\n");
    }

    private void showHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  INSERT INTO users VALUES (1, 'Alice'); - Simple data entry");
        System.out.println("  SELECT * FROM users;                    - View all data");
        System.out.println("  .status                                 - View engine metrics");
        System.out.println("  .exit                                   - Safe shutdown");
        System.out.println();
    }

    private void printHeader() {
        System.out.println("  __  __ _       _ _____  ____  ");
        System.out.println(" |  \\/  (_)     (_)  __ \\|  _ \\ ");
        System.out.println(" | \\  / |_ _ __  _| |  | | |_) |");
        System.out.println(" | |\\/| | | '_ \\| | |  | |  _ < ");
        System.out.println(" | |  | | | | | | | |__| | |_) |");
        System.out.println(" |_|  |_|_|_| |_|_|_____/|____/ ");
        System.out.println("MiniDB Core Engine v1.1 | Nandani-DBMS");
        System.out.println("Type '.help' to see example queries.\n");
    }

    public static void main(String[] args) {
        new Shell().start();
    }
}

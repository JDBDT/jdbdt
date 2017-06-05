package org.jdbdt;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class with methods for database setup.
 * 
 * @since 0.1
 *
 */
final class DBSetup {

  /**
   * Insert a data set onto the database.
   * @param callInfo Call information.
   * @param data Data Set.
   */
  static void insert(CallInfo callInfo, DataSet data) {
    Table table = asTable(data.getSource());
    if (data.isEmpty()) {
      throw new InvalidOperationException("Empty data set.");
    }
    table.setDirtyStatus(true);
    doInsert(callInfo, table, data);
  }

  /**
   * Populate database with a data set.
   * @param callInfo Call Info.
   * @param data Data Set.
   */
  static void populate(CallInfo callInfo, DataSet data) {
    Table table = asTable(data.getSource());
    table.setDirtyStatus(true);
    doPopulate(callInfo, table, data);

  }

  @SuppressWarnings("javadoc")
  private static Table asTable(DataSource source) {
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }
    return (Table) source;
  }

  /**
   * Populate database table with a data set, if associated table
   * has changed.
   * 
   * @param callInfo Call Info.
   * @param data Data Set.
   */
  static void populateIfChanged(CallInfo callInfo, DataSet data) {
    Table table = asTable(data.getSource()); 
    if ( table.getDirtyStatus() ) {
      table.setDirtyStatus(true);
      doPopulate(callInfo, table, data);
    }
  }

  /**
   * Auxiliary method to populate a table.
   * @param callInfo Call info.
   * @param table Table.
   * @param data Data set.
   */
  private static void doPopulate(CallInfo callInfo, Table table, DataSet data) {
    doDeleteAll(callInfo, table);
    doInsert(callInfo, table, data);
    table.setSnapshot(data);
  }

  /**
   * Utility method to perform actual data insertion.
   * @param callInfo Call Info.
   * @param table Table.
   * @param data Data set.
   */
  private static void doInsert(CallInfo callInfo, Table table, DataSet data) {
    final DB db = table.getDB();
    db.logInsertion(callInfo, data);
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    String[] columnNames = table.getColumns();
    sql.append(table.getName())
    .append('(')
    .append(columnNames[0]);
    for (int i=1; i < columnNames.length; i++) {
      sql.append(',').append(columnNames[i]);
    }
    sql.append(") VALUES (?");
    for (int i=1; i < columnNames.length; i++) {
      sql.append(",?");
    }
    sql.append(')');

    db.access(() -> {
      final boolean batchMode = db.useBatchUpdates();
      final int maxBatchSize = db.getMaximumBatchUpdateSize();

      try(WrappedStatement ws = db.compile(sql.toString())) {
        PreparedStatement insertStmt = ws.getStatement();
        int batchSize = 0;
        for (Row r : data.getRows()) {
          final int n = r.length();
          final Object[] cols = r.data();
          if (n != table.getColumnCount()) {
            throw new InvalidOperationException("Invalid number of columns for insertion.");
          }
          for (int i = 0; i < n; i++) {
            insertStmt.setObject(i+1, cols[i]);
          }
          if (batchMode) {
            insertStmt.addBatch();
            batchSize++;
            if (batchSize == maxBatchSize) {
              insertStmt.executeBatch();
              batchSize = 0;
            }
          } else {
            insertStmt.execute();
          }
        }
        if (batchMode && batchSize > 0 ) {
          insertStmt.executeBatch();
        }
      }
      return 0;
    });
  }

  /**
   * Update rows according to data set.
   * 
   * <p>
   * The data set must relate to a table with a defined key (see {@link TableBuilder#key(String...)}.
   * </p>
   * 
   * @param callInfo Call Info.
   * @param data Data set.
   */
  public static void update(CallInfo callInfo, DataSet data) {
    Table table = asTable(data.getSource()); 
    List<String> keyColumns = table.getKeyColumns();
    if (keyColumns.isEmpty()) {
      throw new InvalidOperationException("No key columns defined.");
    }

    String[] tableColumns = table.getColumns();
    List<String> columnsToUpdate = new LinkedList<>();
    columnsToUpdate.addAll(Arrays.asList(tableColumns));
    columnsToUpdate.removeAll(keyColumns);

    if (columnsToUpdate.isEmpty()) {
      throw new InvalidOperationException("No columns to update.");
    }

    DB db = table.getDB();
    db.logUpdate(callInfo, data);

    // Build SQL statement
    StringBuilder sql = new StringBuilder("UPDATE ");
    Iterator<String> itr = columnsToUpdate.iterator();
    sql.append(table.getName())
    .append(" SET ")
    .append(itr.next())
    .append("=?");
    while (itr.hasNext()) {
      sql.append(',')
      .append(itr.next())
      .append("=?");
    }
    itr = keyColumns.iterator();
    sql.append(" WHERE ")
    .append(itr.next())
    .append("=?");
    while (itr.hasNext()) {
      sql.append(" AND ")
      .append(itr.next())
      .append("=?");
    }

    Map<String,Integer> colToStmtArg = new HashMap<>();
    int stmtArgCtr = 1;
    for (String c : columnsToUpdate) {
      colToStmtArg.put(c.toLowerCase(), stmtArgCtr);
      stmtArgCtr++;
    }
    for (String c : keyColumns) {
      colToStmtArg.put(c.toLowerCase(), stmtArgCtr);
      stmtArgCtr++;
    }
    // Perform operation
    table.setDirtyStatus(true);
    //    System.out.println(sql.toString());
    db.access(() -> {
      final boolean batchMode = db.useBatchUpdates();
      final int maxBatchSize = db.getMaximumBatchUpdateSize();

      try(WrappedStatement ws = db.compile(sql.toString())) {
        PreparedStatement updateStmt = ws.getStatement();
        int batchSize = 0;
        for (Row r : data.getRows()) {
          final int n = r.length();
          final Object[] colValues = r.data();
          if (n != table.getColumnCount()) {
            throw new InvalidOperationException("Invalid number of columns for update.");
          }
          for (int c = 0; c < n; c++) {
            //            System.out.printf("%d %s %s\n",  
            //                c,colToStmtArg.get(tableColumns[c].toLowerCase()), colValues[c]);
            updateStmt.setObject(colToStmtArg.get(tableColumns[c].toLowerCase()), colValues[c]);   
          }
          if (batchMode) {
            updateStmt.addBatch();
            batchSize++;
            if (batchSize == maxBatchSize) {
              updateStmt.executeBatch();
              batchSize = 0;
            }
          } else {
            updateStmt.execute();
          }
        }
        if (batchMode && batchSize > 0 ) {
          updateStmt.executeBatch();
        }
      }
      return 0;
    });
  }

  /**
   * Delete rows from data set.
   * 
   * <p>
   * The data set must relate to a table with a defined key (see {@link TableBuilder#key(String...)}.
   * </p>
   * @param callInfo Call Info.
   * @param data Data set.
   */
  public static void delete(CallInfo callInfo, DataSet data) {
    Table table = asTable(data.getSource()); 
    List<String> keyColumns = table.getKeyColumns();
    if (keyColumns.isEmpty()) {
      throw new InvalidOperationException("No key columns defined.");
    }

    DB db = table.getDB();
    db.logDelete(callInfo, data);

    // Build SQL statement
    Iterator<String> itr = keyColumns.iterator();
    StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ")
    .append(table.getName())
    .append(" WHERE ")
    .append(itr.next())
    .append("=?");
    while (itr.hasNext()) {
      sql.append(" AND ")
      .append(itr.next())
      .append("=?");
    }

    Map<String,Integer> colToStmtArg = new HashMap<>();
    int stmtArgCtr = 1;
    for (String c : keyColumns) {
      colToStmtArg.put(c.toLowerCase(), stmtArgCtr);
      stmtArgCtr++;
    }
    // Perform operation
    table.setDirtyStatus(true);
    //      System.out.println(sql.toString());
    db.access(() -> {
      final boolean batchMode = db.useBatchUpdates();
      final int maxBatchSize = db.getMaximumBatchUpdateSize();
      String[] tableColumns = table.getColumns();
      try(WrappedStatement ws = db.compile(sql.toString())) {
        PreparedStatement deleteStmt = ws.getStatement();
        int batchSize = 0;
        for (Row r : data.getRows()) {
          final int n = r.length();
          final Object[] colValues = r.data();
          if (n != table.getColumnCount()) {
            throw new InvalidOperationException("Invalid number of columns for update.");
          }
          for (int c = 0; c < n; c++) {
            //              System.out.printf("%d %s %s\n",  
            //                  c,colToStmtArg.get(tableColumns[c].toLowerCase()), colValues[c]);
            int iParam = colToStmtArg.getOrDefault(tableColumns[c].toLowerCase(), 0);
            if (iParam != 0) {
              deleteStmt.setObject(iParam, colValues[c]);   
            }
          }
          if (batchMode) {
            deleteStmt.addBatch();
            batchSize++;
            if (batchSize == maxBatchSize) {
              deleteStmt.executeBatch();
              batchSize = 0;
            }
          } else {
            deleteStmt.execute();
          }
        }
        if (batchMode && batchSize > 0 ) {
          deleteStmt.executeBatch();
        }
      }
      return 0;
    });
  }

  /**
   * Delete all data from table.
   * @param callInfo Call info.
   * @param table Table.
   * @return Number of deleted rows.
   */
  static int deleteAll(CallInfo callInfo, Table table) {
    table.setDirtyStatus(true);
    return doDeleteAll(callInfo, table);
  }

  /**
   * Perform a "delete-all" operation.
   * @param callInfo Call info.
   * @param table Table.
   * @return Number of deleted rows.
   */
  private static int doDeleteAll(CallInfo callInfo, Table table) {
    DB db = table.getDB();
    return db.access( () -> {
      String sql = "DELETE FROM " + table.getName();
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        return ws.getStatement().executeUpdate();
      }
    });
  }

  /**
   * Truncate table.
   * @param callInfo Call info.
   * @param table Table.
   */
  static void truncate(CallInfo callInfo, Table table) {
    final DB db = table.getDB();   
    db.access(() -> {
      String sql = "TRUNCATE TABLE " + table.getName();
      table.setDirtyStatus(true);
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        ws.getStatement().execute();
      }
      return 0;
    });

  }

  /**
   * Delete all data based on a WHERE clause.
   * @param callInfo Call info.
   * @param table Table.
   * @param where <code>WHERE</code> clause.
   * @param args Optional arguments for <code>WHERE</code> clause.
   * @return The number of deleted rows.
   */
  static int deleteAll(CallInfo callInfo, Table table, String where, Object... args) {
    final DB db = table.getDB();

    return  db.access( () -> {
      table.setDirtyStatus(true);
      String sql = 
          "DELETE FROM " + table.getName() +
          " WHERE " + where;
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        PreparedStatement deleteStmt = ws.getStatement();
        if (args != null && args.length > 0) {
          for (int i=0; i < args.length; i++) {
            deleteStmt.setObject(i + 1, args[i]);
          }
        }
        return deleteStmt.executeUpdate(); 
      }
    });
  }

  /**
   * Drop a table.
   * @param callInfo Call info.
   * @param db Database
   * @param tableName  Table name.
   */
  public static void drop(CallInfo callInfo, DB db, String tableName) {
    String sql = String.format("DROP TABLE %s", tableName);
    db.access(() -> {
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        PreparedStatement dropStmt = ws.getStatement();
        dropStmt.execute();
      }
      return 0; 
    });
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private DBSetup() {

  }



}

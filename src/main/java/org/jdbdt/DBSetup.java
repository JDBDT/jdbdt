package org.jdbdt;

import java.sql.PreparedStatement;

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
   db.access(() -> {
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
   * Private constructor to prevent instantiation.
   */
  private DBSetup() {

  }

}

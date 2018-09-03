/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;

import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class with methods for database setup.
 * 
 * @since 1.0
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
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    List<String> tableColumns = table.getColumns();
    int columnCount = tableColumns.size();
    int[] paramIdx = new int[columnCount];
    int param = 0;
    Iterator<String> itr = tableColumns.iterator();
    String col = itr.next();
    
    paramIdx[param] = ++param;
    sql.append(table.getName())
       .append('(')
       .append(col);
    
    while (itr.hasNext()) {
      paramIdx[param] = ++param;
      col = itr.next();
      sql.append(',')
         .append(col);
    }
    sql.append(") VALUES (?");
    for (int i=1; i < columnCount; i++) {
      sql.append(",?");
    }
    sql.append(')');

    dataSetOperation(callInfo, table, data, sql.toString(), paramIdx);
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

    List<String> tableColumns = table.getColumns();
    int columnCount = tableColumns.size();
    List<String> columnsToUpdate = new LinkedList<>();
    columnsToUpdate.addAll(tableColumns);
    columnsToUpdate.removeAll(keyColumns);

    if (columnsToUpdate.isEmpty()) {
      throw new InvalidOperationException("No columns to update.");
    }

    // Build SQL statement
    int[] paramIdx = new int[columnCount];
    int param = 0;
    StringBuilder sql = new StringBuilder("UPDATE ");
    Iterator<String> itr = columnsToUpdate.iterator();
    String col = itr.next();
    paramIdx[tableColumns.indexOf(col)] = ++param;
    sql.append(table.getName())
       .append(" SET ")
       .append(col)
       .append("=?");
    
    while (itr.hasNext()) {
      col = itr.next();
      paramIdx[tableColumns.indexOf(col)] = ++param;
      sql.append(',')
         .append(col)
         .append("=?");
    }
    
    itr = keyColumns.iterator();
    col = itr.next();
    paramIdx[tableColumns.indexOf(col)] = ++param;
    sql.append(_WHERE_)
       .append(col)
       .append("=?");
    
    while (itr.hasNext()) {
      col = itr.next();
      paramIdx[tableColumns.indexOf(col)] = ++param;
      sql.append(_AND_)
         .append(col)
         .append("=?");
    }
    
    table.setDirtyStatus(true);
    dataSetOperation(callInfo, table, data, sql.toString(), paramIdx);
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
    
    List<String> tableColumns = table.getColumns();
    int param = 0;
    int[] paramIdx = new int[tableColumns.size()];
    Iterator<String> itr = keyColumns.iterator();
    StringBuilder sql = new StringBuilder();
    String kcol = itr.next();
    
    paramIdx[tableColumns.indexOf(kcol)] = ++param;
    sql.append(DELETE_FROM_)
       .append(table.getName())
       .append(_WHERE_)
       .append(kcol)
       .append("=?");
    
    while (itr.hasNext()) {
      kcol = itr.next();
      paramIdx[tableColumns.indexOf(kcol)] = ++param;
      sql.append(_AND_)
         .append(kcol)
         .append("=?");
    }
   
    table.setDirtyStatus(true);
    dataSetOperation(callInfo, table, data, sql.toString(), paramIdx);
  }
  
  @SuppressWarnings("javadoc")
  private static void dataSetOperation(CallInfo callInfo, Table table, DataSet data, String sql, int[] paramIndex) {
    DB db = table.getDB();
    boolean batchMode = db.useBatchUpdates();
    int maxBatchSize = db.getMaximumBatchUpdateSize();
    int columnCount = table.getColumnCount();

    db.logDataSetOperation(callInfo, data);
    
    db.access(callInfo, () -> {
      try(WrappedStatement ws = db.compile(sql)) {
        PreparedStatement stmt = ws.getStatement();
        int batchSize = 0;
        for (Row r : data.getRows()) {
          Object[] colValues = r.data();
          if (colValues.length != columnCount) {
            throw new InvalidOperationException("Invalid number of columns for update.");
          }
          for (int c = 0; c < colValues.length; c++) {
            int iParam = paramIndex[c];
            if (iParam != 0) {
              stmt.setObject(iParam, colValues[c]);   
            }
          }
          if (batchMode) {
            stmt.addBatch();
            batchSize++;
            if (batchSize == maxBatchSize) {
              stmt.executeBatch();
              batchSize = 0;
            }
          } else {
            stmt.execute();
          }
        }
        if (batchMode && batchSize > 0 ) {
          stmt.executeBatch();
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
    return db.access(callInfo, () -> {
      String sql = DELETE_FROM_ + table.getName();
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
    db.access(callInfo, () -> {
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

    return  db.access(callInfo, () -> {
      table.setDirtyStatus(true);
      String sql = 
          DELETE_FROM_ + table.getName() +
          _WHERE_ + where;
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
  static void drop(CallInfo callInfo, DB db, String tableName) {
    String sql = String.format("DROP TABLE %s", tableName);
    db.access(callInfo, () -> {
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        PreparedStatement dropStmt = ws.getStatement();
        dropStmt.execute();
      }
      return 0; 
    });
  }
  
  /**
   * Execute arbitrary SQL code.
   * @param callInfo Call info.
   * @param db Database
   * @param sql SQL statement.
   * @param args Arguments.
   * @return The value obtained through {@link PreparedStatement#getUpdateCount()}, after executing the statement.
   */
  static int execute(CallInfo callInfo, DB db, String sql, Object[] args) {
    return db.access(callInfo, () -> {
      db.logSetup(callInfo, sql);
      try (WrappedStatement ws = db.compile(sql)) {
        PreparedStatement stmt = ws.getStatement();
        if (args != null && args.length != 0) {
          for (int i=0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
          }
        }
        stmt.execute();
        return stmt.getUpdateCount();
      }
    });
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private DBSetup() {

  }
  
  @SuppressWarnings("javadoc")
  private static final String _AND_ = " AND ";
  @SuppressWarnings("javadoc")
  private static final String _WHERE_ = " WHERE ";
  @SuppressWarnings("javadoc")
  private static final String DELETE_FROM_ = " DELETE FROM ";
  
}

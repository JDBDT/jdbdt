package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
   * @throws DBExecutionException If a database error occurs during insertion.
   */
  static void insert(CallInfo callInfo, DataSet data) throws DBExecutionException {
    DataSource source = data.getSource();
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }
    if (data.isEmpty()) {
      throw new InvalidOperationException("Empty data set.");
    }
    insert(callInfo, (Table) source, data);
  }

  /**
   * Populate database with a data set.
   * @param callInfo Call Info.
   * @param data Data Set.
   * @throws DBExecutionException If a database error occurs.
   */
  static void populate(CallInfo callInfo, DataSet data) 
  throws DBExecutionException {
    DataSource source = data.getSource();
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }

    Table table = (Table) source;
    deleteAll(callInfo, table);
    insert(callInfo, table, data);
    table.setSnapshot(data);
  }

  /**
   * Utility method to perform actual data insertion.
   * @param callInfo Call Info.
   * @param table Table.
   * @param data Data set.
   * @throws DBExecutionException If a database error occurs.
   */
  private static void insert(CallInfo callInfo, Table table, DataSet data)
  throws DBExecutionException {
    try {
      DB db = table.getDB();
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
      PreparedStatement  insertStmt = db.compile(sql.toString());
      for (Row r : data.getRows()) {
        final int n = r.length();
        final Object[] cols = r.data();
        if (n != table.getColumnCount()) {
          throw new InvalidOperationException("Invalid number of columns for insertion.");
        }
        for (int i = 0; i < n; i++) {
          insertStmt.setObject(i+1, cols[i]);
        }
        insertStmt.execute();
        insertStmt.clearParameters();
      }
    }
    catch(SQLException e) {
      throw new DBExecutionException(e);
    }
  }

  /**
   * Delete all data from table.
   * @param callInfo Call info.
   * @param table Table.
   * @return Number of deleted rows.
   * @throws DBExecutionException If a database error occurs.
   */
  static int deleteAll(CallInfo callInfo, Table table) 
  throws DBExecutionException {
    try {
      String sql = "DELETE FROM " + table.getName();
      DB db = table.getDB();
      db.logSetup(callInfo, sql);
      return db.compile(sql).executeUpdate();
    } 
    catch (SQLException e) {
      throw new DBExecutionException(e);
    }
  }

  /**
   * Truncate table.
   * @param callInfo Call info.
   * @param table Table.
   * @throws DBExecutionException If a database error occurs.
   */
  static void truncate(CallInfo callInfo, Table table) 
  throws DBExecutionException {
    try {
      String sql = "TRUNCATE TABLE " + table.getName();
      DB db = table.getDB();
      db.logSetup(callInfo, sql);
      db.compile(sql).execute();
    }
    catch (SQLException e) {
      throw new DBExecutionException(e);
    }
  }

  /**
   * Delete all data based on a WHERE clause.
   * @param callInfo Call info.
   * @param table Table.
   * @param where <code>WHERE</code> clause.
   * @param args Optional arguments for <code>WHERE</code> clause.
   * @return The number of deleted rows.
   * @throws DBExecutionException If a database error occurs.
   */
  static int deleteAll(CallInfo callInfo, Table table, String where, Object... args) 
  throws DBExecutionException {
    try {
      String sql = 
        "DELETE FROM " + table.getName() +
        " WHERE " + where;
      
      DB db = table.getDB();
      db.logSetup(callInfo, sql);
      PreparedStatement deleteStmt = db.compile(sql);
      if (args != null && args.length > 0) {
        for (int i=0; i < args.length; i++) {
          deleteStmt.setObject(i + 1, args[i]);
        }
      }
      return deleteStmt.executeUpdate(); 
    }
    catch (SQLException e) {
      throw new DBExecutionException(e);
    }
    
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private DBSetup() {

  }
  
}

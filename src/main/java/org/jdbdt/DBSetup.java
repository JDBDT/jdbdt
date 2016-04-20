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
   * @throws SQLException If a database error occurs during insertion.
   */
  static void insert(CallInfo callInfo, DataSet data) throws SQLException {
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
   * @throws SQLException If a database error occurs during insertion.
   */
  static void populate(CallInfo callInfo, DataSet data) throws SQLException {
    DataSource source = data.getSource();
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }
    if (data.isEmpty()) {
      throw new InvalidOperationException("Empty data set.");
    }
    Table t = (Table) source;
    deleteAll(t);
    insert(callInfo, t, data);
    t.setSnapshot(data);
  }
  
  /**
   * Utility method to perform actual data insertion.
   * @param callInfo Call Info.
   * @param table Table.
   * @param data Data set.
   * @throws SQLException If a database error occurs.
   */
  private static void insert(CallInfo callInfo, Table table, DataSet data) throws SQLException {
    table.getDB().logInsertion(callInfo, data);
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
    PreparedStatement  insertStmt = table.getDB().compile(sql.toString());
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
  
  /**
   * Delete all data from table.
   * @param t Table.
   * @return Number of deleted rows.
   * @throws SQLException If a database error occurs.
   */
  static int deleteAll(Table t) throws SQLException {
    return t.getDB()
            .compile("DELETE FROM " + t.getName())
            .executeUpdate();
  }
  
  /**
   * Truncate table.
   * @param t Table.
   * @throws SQLException If a database error occurs.
   */
  static void truncate(Table t) throws SQLException {
    t.getDB()
     .compile("TRUNCATE TABLE " + t.getName())
     .execute();
  }
  
  /**
   * Delete all data based on a WHERE clause.
   * @param table Table.
   * @param where <code>WHERE</code> clause.
   * @param args Optional arguments for <code>WHERE</code> clause.
   * @return The number of deleted rows.
   * @throws SQLException If a database error occurs.
   */
  static int deleteAll(Table table, String where, Object... args) throws SQLException {
    PreparedStatement deleteStmt = 
      table.getDB().compile(
        "DELETE FROM " + table.getName() +
        " WHERE " + where);
    if (args != null && args.length > 0) {
      for (int i=0; i < args.length; i++) {
        deleteStmt.setObject(i + 1, args[i]);
      }
    }
    return deleteStmt.executeUpdate(); 
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private DBSetup() {
    
  }
}

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
   * @param data Data Set.
   * @throws SQLException If a database error occurs during insertion.
   */
  static void insert(DataSet data) throws SQLException {
    DataSource source = data.getSource();
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }
    if (data.isEmpty()) {
      throw new InvalidOperationException("Empty data set.");
    }
    insert((Table) source, data);
  }
  
  /**
   * Populate database with a data set.
   * @param data Data Set.
   * @throws SQLException If a database error occurs during insertion.
   */
  static void populate(DataSet data) throws SQLException {
    DataSource source = data.getSource();
    if ( ! (source instanceof Table)) {
      throw new InvalidOperationException("Data set is not defined for a table.");
    }
    if (data.isEmpty()) {
      throw new InvalidOperationException("Empty data set.");
    }
    Table t = (Table) source;
    deleteAll(t);
    insert(t, data);
    t.setSnapshot(data);
  }
  
  /**
   * Utility method to perform actual data insertion.
   * @param t Table.
   * @param data Data set.
   * @throws SQLException If a database error occurs.
   */
  private static void insert(Table t, DataSet data) throws SQLException {
    t.getDB().logInsertion(data);
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    String[] columnNames = t.getColumns();
    sql.append(t.getName())
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
    PreparedStatement  insertStmt = t.getDB().compile(sql.toString());
    for (Row r : data) {
      final int n = r.length();
      final Object[] cols = r.data();
      if (n != t.getColumnCount()) {
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
   * Delete all data based on a query's WHERE clause.
   * @param q Query.
   * @return The number of deleted rows.
   * @throws SQLException If a database error occurs.
   */
  static int deleteAll(Query q) throws SQLException {
    if (q.groupByClause() != null) {
      throw new InvalidOperationException("GROUP BY clause is set!");
    }
    if (q.havingClause() != null) {
      throw new InvalidOperationException("HAVING clause is set!");
    }
    String whereClause = q.whereClause();

    if (whereClause == null) {
      throw new InvalidOperationException("WHERE clause is not set!");
    }
    if (q.fromClause().length != 1) {
      throw new InvalidOperationException("FROM clause specifies multiple data sources!");
    }
    PreparedStatement deleteStmt = 
      q.getDB().compile(
        "DELETE FROM " + q.fromClause()[0] +
        " WHERE " + whereClause);
    Object[] args = q.getQueryArguments();
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

package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Database table representation.
 * 
 * 
 * <p>
 * An object of this kind models access to a database table 
 * An instance of {@link Table}:
 * </p>
 * <ul>
 * <li>
 * must be created using {@link JDBDT#table(String)};
 * </li>
 * <li>
 * may have an associated subset of the table's columns using {@link #columns(String...)} - 
 * otherwise, by default, all table columns will be considered;
 * </li>
 * <li>
 * must be associated (only once) to a database connection using 
 * {@link #boundTo(Connection)} before any actual database operations.
 * </li>
 * </ul>
 * 
 * <p><b>Illustration</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Table;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Table t = table("TableName")
 *          .columns("A", "B", "C", "D")
 *          .boundTo(conn);
 * </pre></blockquote>
 * 
 * 
 * @see TypedTable
 * @since 0.1
 */
public class Table extends SnapshotProvider {

  /**
   * Table name.
   */
  private final String tableName;

  /**
   * Table columns (if null, all columns will be considered).
   */
  private String[] columnNames = null;

  /**
   * Connection to which the table is bound.
   */
  private Connection connection = null;


  /**
   * Constructor.
   * 
   * @param tableName Table name
   * @see JDBDT#table(String)
   */
  Table(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Get table name.
   * @return The name of the table.
   */
  public final String getName() {
    return tableName;
  }

  /** 
   * Specify database columns to consider for the table. 
   * 
   * @param columnNames Column names.
   * @return The table instance (for chained calls).
   */
  public Table columns(String... columnNames) {
    checkIfNotBound();
    if (this.columnNames != null) {
      throw new InvalidUsageException("Columns are already defined.");
    }
    this.columnNames = columnNames.clone();
    return this;
  }

  /**
   * Get column names.
   * @return Array of column names, or <code>null</code> if all
   *   columns are set.
   */
  final String[] getColumnNames() {
    return columnNames;
  }


  /**
   * Get column count.
   * @return -1 if the columns were not defined, 
   *         a positive integer value otherwise.
   * @see #columns(String...)
   */
  final int getColumnCount() {
    return columnNames == null ? -1 : columnNames.length;
  }

  /**
   * Set database connection to use.
   * @param conn Database connection.
   * @return The table instance (for chained calls).
   * @throws SQLException If a database error occurs.
   */
  public Table boundTo(Connection conn) throws SQLException {
    checkIfNotBound();
    connection = conn;
    compileQuery();
    return this;
  }
  
  @Override
  public String getSQLForQuery() {
    StringBuilder sql = new StringBuilder("SELECT\n ");
    if (columnNames ==  null) {
      sql.append('*');
    } else {
      sql.append(columnNames[0]);
      for (int i = 1; i < columnNames.length;i++) {
        sql.append(',').append('\n').append(' ').append(columnNames[i]);
      }
    }
    sql.append("\nFROM ").append(tableName);
    return sql.toString();
  }
  
  /**
   * Get connection associated to this table.
   * 
   * @see #boundTo(Connection)
   * @return The database connection instance.
   */
  @Override
  final Connection getConnection() {
    checkIfBound();
    return connection;
  }

 
  /**
   * Ensure table is bound to a connection,
   * otherwise throw {@link InvalidUsageException}.
   */
  private void checkIfBound() {
    if (connection == null) {
      throw new InvalidUsageException("Table is not bound to a connection.");
    }
  }
  /**
   * Ensure table is NOT bound to a connection,
   * otherwise throw {@link InvalidUsageException}.
   */
  private void checkIfNotBound() {
    if (connection != null) {
      throw new InvalidUsageException("Table is already bound to a connection.");
    }
  }
 
  /**
   * Insert a row into the table.
   * @param row Row to insert.
   * @throws SQLException If a dababase error occurs.
   * @throws InvalidUsageException if the number of columns in the row 
   *    is different from expected. 
   */
  void insertRow(Object[] row) throws SQLException {
    final int n = getColumnCount();
    if ( row.length != n) {
      throw new InvalidUsageException("Expected "+ n + 
          " column values, got " +  row.length);  
    }
    PreparedStatement  insertStmt = StatementPool.insert(this);
    for (int i = 1; i <= n; i++) {
      insertStmt.setObject(i, row[i-1]);
    }
    insertStmt.execute();
    insertStmt.clearParameters();
  }

 
}

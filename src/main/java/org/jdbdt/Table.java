package org.jdbdt;

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
 * must be created using {@link DB#table(String)};
 * </li>
 * <li>
 * may have an associated subset of the table's columns using {@link #columns(String...)} - 
 * otherwise, by default, all table columns will be considered;
 * </li>
 * </ul>
 * 
 * <p><b>Illustration</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Table;
 * import java.sql.Connection;
 * ...
 * DB db = ...;
 * Table t = table("TableName")
 *          .columns("A", "B", "C", "D");
 * </pre></blockquote>
 * 
 * @since 0.1
 */
public final class Table extends DataSource {
  /**
   * Table name.
   */
  private final String tableName;

  /**
   * Table columns (if null, all columns will be considered).
   */
  private String[] columnNames = null;

  /**
   * Constructor.
   * @param db Database instance.
   * @param tableName Table name
   * @see DB#table(String)
   */
  Table(DB db, String tableName) {
    super(db);
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
  @Override
  final String[] getColumnNames() {
    return columnNames;
  }

  
  @Override
  String getSQLForQuery() {
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

  @Override
  final Object[] getQueryArguments() {
    return null;
  }
}

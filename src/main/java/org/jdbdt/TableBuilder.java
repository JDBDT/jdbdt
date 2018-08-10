package org.jdbdt;


/**
 * Table builder.
 * 
 * 
 * @see Table
 * 
 * @since 0.8
 */
public final class TableBuilder  {
  /**
   * Constant for selecting all columns.
   */
  private static final String[] ALL_COLUMNS = { "*" };
  
  /**
   * Name.
   */
  private String tableName;
  
  /**
   * Columns.
   */
  private String[] tableColumns;
  
  /**
   * Columns.
   */
  private String[] keyColumns;

  /**
   * Constructs a new table builder.
   */
  public TableBuilder() {
    tableName = null;
    tableColumns = null;
  }
  
  /**
   * Set table name. 
   * @param name Name for the table.
   * @return The builder instance for chained calls.
   */
  public TableBuilder name(String name) {
    if (tableName != null) {
      throw new InvalidOperationException("The table name is already defined.");
    }
    tableName = name;
    return this;
  }
  
  /**
   * Set columns. 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  @SafeVarargs
  public final TableBuilder columns(String... columns) {
    if (columns == null || columns.length == 0) {
      throw new InvalidOperationException("Columns array is null or empty.");
    }
    if (tableColumns != null) {
      throw new InvalidOperationException("The table columns are already defined.");
    }
    tableColumns = columns.clone();
    return this;
  }
  
  /**
   * Set key columns.
   * 
   * <p>
   * Values for the key columns are used to uniquely identify database rows, e.g., they 
   * can be set to the columns that define the table's primary key. The key
   * columns are used by key-based operations like {@link JDBDT#delete(DataSet)}
   * and {@link JDBDT#update(DataSet)}.
   * 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  @SafeVarargs
  public final TableBuilder key(String... columns) {
    if (columns == null || columns.length == 0) {
      throw new InvalidOperationException("Columns array is null or empty.");
    }
    if (keyColumns != null) {
      throw new InvalidOperationException("The key columns are already defined.");
    }
    keyColumns = columns.clone();
    return this;
  }
  /**
   * Build the table object.
   * @param db Database.
   * @return A new {@link Table} instance.
   */
  public Table build(DB db) {
    if (tableName == null) {
      throw new InvalidOperationException("The table name has not been set.");
    }
    if (tableColumns == null) {
      tableColumns = ALL_COLUMNS;
    }
    return new Table(CallInfo.create(), db, tableName, tableColumns, keyColumns); 
  }

}

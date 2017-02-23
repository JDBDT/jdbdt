package org.jdbdt;

/**
 * Table data source.
 * 
 * @see JDBDT#table(DB, String)
 * @since 0.1
 */
public final class Table extends DataSource {
  /**
   * Table name.
   */
  private final String tableName;

  /**
   * Dirty status flag.
   */
  private boolean dirty;
  /**
   * Constructor.
   * @param db Database instance.
   * @param tableName Table name
   * @see JDBDT#table(DB,String)
   */
  Table(DB db, String tableName) {
    super(db);
    this.tableName = tableName;
    this.dirty = true;
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
   * @param columns SQL columns.
   * @return The table instance (for chained calls).
   */
  public Table columns(String... columns) {
    super.setColumns(columns);
    return this;
  }

  /**
   * Set dirty status.
   * 
   * <p>
   * This method is used by {@link JDBDT#populateIfChanged}
   * and {@link JDBDT#assertUnchanged(DataSource)}.
   * </p>
   * 
   * @param dirty Status.
   */
  void setDirtyStatus(boolean dirty) {
    this.dirty = dirty;
  }
  
  /**
   * Get dirty status.
   * 
   * <p>
   * This method is used by {@link JDBDT#populateIfChanged}
   * to check if the table has a dirty status.
   * </p>.
   * 
   * @return The dirty status.
   */
  boolean getDirtyStatus() {
    return dirty;
  }
  
  @Override
  public String getSQLForQuery() {
    StringBuilder sql = new StringBuilder("SELECT ");
    String[] columns = getColumns();
    if (columns == null) {
      sql.append('*');
    } else {
      sql.append(columns[0]);
      for (int i = 1; i < columns.length;i++) {
        sql.append(',').append(' ').append(columns[i]);
      }
    }
    sql.append(" FROM ").append(tableName);
    return sql.toString();
  }

  
}

package org.jdbdt;

/**
 * Table data source.
 * 
 * @see JDBDT#table(String)
 * @since 0.1
 */
public final class Table extends DataSource {
  /**
   * Table name.
   */
  private final String tableName;

  /**
   * Constructor.
   * @param db Database instance.
   * @param tableName Table name
   * @param sql SQL for table query.
   * @see JDBDT#table(String)
   */
  Table(DB db, String tableName, String sql) {
    super(db, sql, new Object[0]);
    this.tableName = tableName;
  }

  /**
   * Get table name.
   * @return The name of the table.
   */
  public String getName() {
    return tableName;
  }  
}

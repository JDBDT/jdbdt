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
   * @param name Table name
   * @param columns Database columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  public Table(DB db, String name, String[] columns) {
    super(db, 
          String.format("SELECT %s FROM %s", 
                         Misc.sqlArgumentList(columns), name));

    tableName = name;
  }

  /**
   * Get table name.
   * @return The name of the table.
   */
  public String getName() {
    return tableName;
  }
}

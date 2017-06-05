package org.jdbdt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
   * Key columns.
   */
  private List<String> keyColumns;

  /**
   * Constructor.
   * @param db Database instance.
   * @param name Table name
   * @param columns Database columns.
   * @param key Key columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  public Table(DB db, String name, String[] columns, String[] key) {
    super(db, 
          String.format("SELECT %s FROM %s", 
                         Misc.sqlArgumentList(columns), name));

    tableName = name;
    keyColumns = key != null ? 
         Collections.unmodifiableList(Arrays.asList(key))
       : Collections.emptyList();
  }

  /**
   * Get table name.
   * @return The name of the table.
   */
  public String getName() {
    return tableName;
  }

  /**
   * Get key columns.
   * @return Key columns.
   */
  public List<String> getKeyColumns() {
    return keyColumns;
  }
}

package org.jdbdt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Table data source.
 * 
 * @see JDBDT#table(String)
 * @since 1.0
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
   * @param keyColumns Key columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  public Table(DB db, String name, String[] columns, String[] keyColumns) {
    this(CallInfo.create(), db, name, columns, keyColumns);
  }
  
  /**
   * Constructor with supplied call info.
   * @param callInfo Call info.
   * @param db Database instance.
   * @param name Table name
   * @param columns Database columns.
   * @param keyCols Key columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  Table(CallInfo callInfo, DB db, String name, String[] columns, String[] keyCols) {
    super(callInfo, db, 
          String.format("SELECT %s FROM %s", 
                         Misc.sqlArgumentList(columns), name));

    tableName = name;
    keyColumns = keyCols != null ? 
         Collections.unmodifiableList(Arrays.asList(keyCols))
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

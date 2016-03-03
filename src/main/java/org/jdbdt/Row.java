package org.jdbdt;

/**
 * Database row.
 * 
 * @since 0.1
 *
 */
public interface Row {

  /**
   * Get column count.
   * @return the number of columns in the database row.
   */
  int getColumnCount();
  
  /**
   * Get column data.
   * @return An array of objects. 
   *   Each entry in the array corresponds to a database column.
   */
  Object[] getColumnData();
}

package org.jdbdt;

/**
 * Database row.
 * 
 * @since 0.1
 *
 */
public interface Row {

  /**
   * Get row length.
   * @return the number of columns in the database row.
   */
  int length();
  
  /**
   * Get row data.
   * @return An array of objects. 
   *   Each entry in the array corresponds to a database column.
   */
  Object[] data();
}

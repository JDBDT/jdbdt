package org.jdbdt;

import java.util.Arrays;

/**
 * Database row representation.
 * 
 * <p>
 * This class is used internally by JDBDT
 * to represent database rows.
 * </p>
 * 
 * @since 0.1
 */
final class Row {
  /**
   * Data.
   */
  private final Object[] data;
  /**
   * Hash code (computed only once).
   */
  private volatile int hash; 
  
  /**
   * Constructs a new row.
   * @param data Values for the columns of this row.
   */
  Row(Object[] data) {
    this.data = data;
    this.hash = 0;
  }
  
  /**
   * Get data for this row.
   * @return The data representing the columns of this row.
   */
  Object[] data() {
    return data;
  }
  
  /**
   * Get row length.
   * @return the number of columns in the database row.
   */
  int length() {
    return data.length;
  }
  
  /**
   * Test for equality.
   * @return <code>true</code> if <code>o</code> represents a row with the same data.
   */
  @Override
  public boolean equals(Object o) {
    return  o == this 
        || (o instanceof Row && Arrays.equals(data, ((Row) o).data));
  }
  
  /**
   * Get hash code.
   * 
   * 
   * @return Hash code for this row obtained by calling 
   *        {@link Arrays#hashCode()} over {@link #data()}.
   *        The returned value is calculated only once and cached for subsequent.
   *        
   */
  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      hash = h = Arrays.hashCode(data);
    }
    return h;
  }
  
  /**
   * Get string representation for this row.
   * @return String obtained by calling {@link Arrays#toString()} over {@link #data()}.
   */
  @Override
  public String toString() {
    return Arrays.toString(data);
  }

}
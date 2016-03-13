package org.jdbdt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Representation of a row set.
 * 
 * <p>
 * This class is used internally by JDBDT to represent database row sets.
 * </p>
 * 
 * @since 0.1
 *
 */
final class DataSet implements Iterable<Row> {
  
  /**
   * Rows in the data set.
   */
  private final ArrayList<Row> rows;
  
  /**
   * Constructs a new data set.
   */
  DataSet() {
    this(new ArrayList<>());
  }
  
  /**
   * Constructs a new row set.
   * @param l Row list. 
   */
  DataSet(ArrayList<Row> l) {
    this.rows = l;
  }
  
  
  /**
   * Test if set is empty.
   * @return <code>true</code> is  set is empty.
   */
  boolean isEmpty() {
    return rows.isEmpty();
  }
  
  /**
   * Get size set.
   * @return The number of rows in the set.
   */
  int size() {
    return rows.size();
  }
  
  /**
   * Clear contents.
   */
  void clear() {
    rows.clear();
  }
  

  /**
   * Add a row to the set.
   * @param r Row to add.
   */
  void addRow(Row r) {
    rows.add(r);
  }
  

  
  @Override
  public boolean equals(Object o) {
    return o == this 
        || (    o instanceof DataSet 
            &&  rows.equals( ((DataSet) o).rows) );
  }

  /**
   * Get an iterator for the row set.
   * @return An iterator object.
   */
  @Override
  public Iterator<Row> iterator() {
    return rows.iterator();
  }
}

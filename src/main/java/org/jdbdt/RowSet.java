package org.jdbdt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
final class RowSet implements Iterable<Row> {
  
  /**
   * Rows in the data set.
   */
  private final LinkedHashMap<Row,Integer> rows;
  
  /**
   * Constructs a new set.
   */
  RowSet() {
    this.rows = new LinkedHashMap<>();
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
   * Get set view of the rows.
   * @return The rows in this set as a set view.
   */
  Set<Map.Entry<Row, Integer>> rows() {
    return rows.entrySet();
  }
  
  /**
   * Test if set contains a row.
   * @param r Row
   * @return <code>true</code> the set contains the row.
   */
  boolean containsRow(Row r) {
    return rows.containsKey(r);
  }
  
  /**
   * Get number of instances of a row.
   * @param r Row.
   * @return Number of instances of the row in the set.
   */
  int instances(Row r) {
    return rows.getOrDefault(r, 0);
  }
  
  /**
   * Add a row to the set.
   * @param r Row to add.
   */
  void addRow(Row r) {
    rows.put(r, rows.getOrDefault(r, 0) + 1);
  }
  
  /**
   * Remove a row from the set.
   * @param r Row to removed.
   * @return <code>true</code> if the row was part of the set.
   *   and was removed.
   */
  boolean removeRow(Row r) {
    boolean removed = true;
    int n = rows.getOrDefault(r, 0);
    switch (n) {
      case 0:  removed = false;  break;
      case 1:  rows.remove(r);   break;
      default: rows.put(r, n-1); break;
    }
    return removed;
  }
  
  /**
   * Compute new set that represents
   * the difference between this set and a given set.
   * @param other Other data set.
   * @return A data set formed by rows in this data set,
   *    that are not part of the other data set.
   */
  RowSet diff(RowSet other) {
    RowSet diff = new RowSet();
    LinkedHashMap<Row,Integer> dset = diff.rows;
    for (Map.Entry<Row,Integer> e : rows()) {
      Row r = e.getKey();
      int d = e.getValue() - other.instances(r);
      if (d > 0) {
        dset.put(r, d);
      }
    }
    return diff;
  }
  
  @Override
  public boolean equals(Object o) {
    return o == this 
        || (    o instanceof RowSet 
            &&  rows.equals( ((RowSet) o).rows) );
  }

  /**
   * Get an iterator for the row set.
   * @return An iterator object.
   */
  @Override
  public Iterator<Row> iterator() {
    return new Iterator<Row>() {
      Iterator<Entry<Row,Integer>> internalItr 
        = rows.entrySet().iterator();
      int itemCount = 0;
      Entry<Row,Integer> lastEntry = null;
      @Override
      public boolean hasNext() {
        return itemCount > 0 || internalItr.hasNext();
      }

      @Override
      public Row next() {
        if (itemCount == 0) {
          lastEntry = internalItr.next();
          itemCount = lastEntry.getValue();
        } 
        itemCount--;
        return lastEntry.getKey();
      }
    };
  }
}

package org.jdbdt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

/**
 * Delta between row sets.
 * 
 * @since 0.1
 *
 */
final class Delta {

  /**
   * Storage of differences. 
   * A map is used to keep row counts because row sets are in fact multi-sets.
   */
  private final LinkedHashMap<Row, Integer> diff = new LinkedHashMap<>();
  
  /**
   * Constructs a new delta.
   * @param ref Reference data set.
   * @param upd Updated data set.
   */
  public Delta(DataSet ref, DataSet upd) {
    this(ref.getRows().iterator(), upd.getRows().iterator());
  }
  
  /**
   * Constructs a new delta.
   * @param ref Iterator for reference data.
   * @param upd Iterator for updated data.
   */
  Delta(Iterator<Row> ref, Iterator<Row> upd) {
    boolean done = false;
    while (!done) {
      if (!ref.hasNext()) {
        while (upd.hasNext()) {
          update(upd.next(), +1);
        }
        done = true;
      }
      else if (!upd.hasNext()) {
        while (ref.hasNext()) {
          update(ref.next(), -1);
        }
        done = true;
      } 
      else {
        update(ref.next(), -1);
        update(upd.next(), +1);
      }
    }
  }

  @SuppressWarnings("javadoc")
  private void 
  update(Row r, int d) {
    int n = diff.getOrDefault(r, 0) + d;
    if (n == 0) {
      diff.remove(r);
    }
    else {
      diff.put(r, n);
    } 
  }
  
  /**
   * Check if the delta is empty, i.e., 
   * if there were no differences found.
   * @return <code>true</code> if the delta is empty.
   */
  boolean isEmpty() {
    return diff.isEmpty();
  }
  /**
   * Get iterator for deleted data.
   * @return Iterator that allows the traversal of deleted rows.
   */
  Iterator<Row> deleted() {
    return new DeltaIterator(diff, true);
  }
  
  /**
   * Get iterator for inserted data.
   * @return Iterator that allows the traversal of deleted rows.
   */
  Iterator<Row> inserted() {
    return new DeltaIterator(diff, false);
  }
  
  
  @SuppressWarnings("javadoc")
  private static
  class DeltaIterator implements Iterator<Row> {
    Iterator<Entry<Row,Integer>> supportItr;
    Entry<Row,Integer> nextEntry;
    boolean iterateOldData;
    int count;

    DeltaIterator(LinkedHashMap<Row, Integer> diff, boolean deletedData) {
      supportItr = diff.entrySet().iterator();
      iterateOldData = deletedData;
      count = 0;
      nextEntry = null;
      advance();
    }

    private void advance() {
      if (count == 0) {
        nextEntry = null;
        while (supportItr.hasNext() && nextEntry == null) {
          Entry<Row,Integer> entry = supportItr.next();
          if (entry.getValue() < 0) {
            if (iterateOldData) {
              nextEntry = entry; 
            }
          }
          else if (!iterateOldData) {
            nextEntry = entry;
          }
        }

      }
    }

    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }

    @Override
    public Row next() {
      if (nextEntry == null) {
        throw new NoSuchElementException();
      }
      Row r = nextEntry.getKey();
      advance();
      return r;
    }
  }
}

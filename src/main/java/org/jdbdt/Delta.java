package org.jdbdt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * Database delta.
 * 
 * @since 0.1
 * 
 */
final class Delta {
  /**
   * Data source.
   */
  private final DataSource source;

  /**
   * Representation of differences.
   */
  private final LinkedHashMap<Row, Integer> diff;

  /**
   * Constructs a new database delta.
   * @param callInfo Call info.
   * @param s Data source.
   */
  Delta(CallInfo callInfo, DataSource s) { 
    this(callInfo, s.getSnapshot());
  }

  /**
   * Construct a new delta.
   * @param callInfo Call info.
   * @param pre Pre-state to assume.
   */
  Delta(CallInfo callInfo, DataSet pre) {
    source = pre.getSource();
    diff = calcDiff(pre, source.executeQuery(callInfo, false));
    source.getDB().logDelta(this);
  }

  /**
   * Get meta-data.
   * @return the meta-data associated to this delta.
   */
  MetaData getMetaData() {
    return source.getMetaData();
  }
  
  /**
   * Get data source for delta.
   * @return The data source instance associated to this delta.
   */
  DataSource getSource() {
    return source;
  }

  /**
   * Get delta size. 
   * 
   * <p>
   * The method returns the number of unverified changes.
   * </p>
   * 
   * @return The size of the delta left to verify (0 if all
   * changes were verified).
   */
  public int size() {
    return diff.size();
  }

  /**
   * Assert that the given row is no longer defined.
   * 
   * @param row Column values defining the pre-existing database row.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case the row is still defined.
   *          
   */
  public Delta before(Object... row) throws DBAssertionError {
    before(new Row(row));
    return this;
  }

  /**
   * Assert that the given row is now defined.
   * 
   * @param row Column values for expected new row.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case the new row is not defined.
   */
  public Delta after(Object... row) throws DBAssertionError {
    after(new Row(row));
    return this;
  }

  /**
   * Assert that the given data set is no longer defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case some row in the data set is still defined.         
   */
  public Delta before(DataSet data)  throws DBAssertionError {
    for (Row r : data.getRows()) {
      before(r);
    }
    return this;
  }

  /**
   * Assert that the given data set is now defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case some row in the data set is still defined.    
   */
  public Delta after(DataSet data)  throws DBAssertionError {
    for (Row r : data.getRows()) {
      after(r);
    }
    return this;
  }

  /**
   * End verification.
   * 
   * <p>
   * A call to this method will assert that there are 
   * no database changes left to verify.
   * If there are any unverified changes, 
   * {@link DBAssertionError} will be thrown.
   * </p>
   * 
   * @throws DBAssertionError in case 
   *         there are remaining changes to verify
   */
  public void end() throws DBAssertionError {
    if (size() > 0) {
      throwDeltaAssertionError(size() + " unverified changes.");
    }
  }

  @SuppressWarnings("javadoc")
  private void after(Row r) {
    int n = diff.getOrDefault(r, 0);
    if (n <= 0) {
      throwDeltaAssertionError("New query result expected: "
          + r.toString());
    }
    --n;
    if (n == 0) {
      diff.remove(r);
    } 
    else {
      diff.put(r, n);
    }
  }

  @SuppressWarnings("javadoc")
  private void before(Row r) {
    int n = diff.getOrDefault(r, 0);
    if (n >= 0) {
      throwDeltaAssertionError("Old query result expected: "
          + r.toString());
    }
    ++n;
    if (n == 0) {
      diff.remove(r);
    } 
    else {
      diff.put(r, n);
    }
  }

  /**
   * Throw {@link DBAssertionError} and,
   * if logging is enabled, write delta to log.
   * 
   * @param msg Error message.
   * @throws DBAssertionError in any case (unconditionally).
   */
  private void throwDeltaAssertionError(String msg) {
    // TODO logging
    throw new DBAssertionError(msg);
  }

  /**
   * Calculate differences between two sets of rows.
   * @param rs1 First set.
   * @param rs2 Second set.
   * @return Map reflecting differences between both sets.
   */
  static
  LinkedHashMap<Row, Integer> calcDiff(DataSet rs1, DataSet rs2) {
    LinkedHashMap<Row,Integer> diff = new LinkedHashMap<>();
    // Try to minimize space use by matching equal rows soon.
    Iterator<Row> 
      a = rs1.getRows().iterator(),
      b = rs2.getRows().iterator();
    boolean done = false;
    while (!done) {
      if (!a.hasNext()) {
        while (b.hasNext()) {
          update(diff, b.next(), +1);
        }
        done = true;
      }
      else if (!b.hasNext()) {
        while (a.hasNext()) {
          update(diff, a.next(), -1);
        }
        done = true;
      } 
      else {
        update(diff, a.next(), -1);
        update(diff, b.next(), +1);
      }
    }

    return diff;
  }

  @SuppressWarnings("javadoc")
  private static void 
  update(LinkedHashMap<Row, Integer> diff, Row r, int d) {
    int n = diff.getOrDefault(r, 0) + d;
    if (n == 0) {
      diff.remove(r);
    }
    else {
      diff.put(r, n);
    } 
  }


  /**
   * Get iterator for 'before' set.
   * @return An iterator instance.
   */
  Iterator<Row> bIterator () {
    return new DeltaIterator(diff, true);
  }

  /**
   * Get iterator for 'after' set.
   * @return An iterator instance.
   */
  Iterator<Row> aIterator () {
    return new DeltaIterator(diff, false);
  }

  @SuppressWarnings("javadoc")
  private static
  class DeltaIterator implements Iterator<Row> {
    Iterator<Entry<Row,Integer>> supportItr;
    Entry<Row,Integer> nextEntry;
    boolean preState;
    int count;

    DeltaIterator(LinkedHashMap<Row, Integer> diff, boolean pre) {
      supportItr = diff.entrySet().iterator();
      preState = pre;
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
            if (preState) {
              nextEntry = entry; 
            }
          }
          else if (!preState) {
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

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
   * Iterator type for a delta object.
   * 
   * @since 0.1
   */
  enum IteratorType {
    /**
     * Actual new data.
     */
    ACTUAL_NEW_DATA,
    /**
     * Actual old data. 
     */
    ACTUAL_OLD_DATA,
    /**
     * Expected new data.
     */
    EXPECTED_NEW_DATA,
    /**
     * Expected old data.
     */
    EXPECTED_OLD_DATA
  }
  
  /**
   * Data source.
   */
  private final DataSource source;

  /**
   * Reference snapshot.
   */
  private final DataSet snapshot;
  
  /**
   * Updated database state.
   */
  private final DataSet state;
  
  /**
   * Representation of differences.
   */
  private final LinkedHashMap<Row, Integer> diff = new LinkedHashMap<>();

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
   * @param data Reference snapshot.
   */
  Delta(CallInfo callInfo, DataSet data) {
    source = data.getSource();
    this.snapshot = data;
    this.state = source.executeQuery(callInfo, false);
    deriveDelta();
    source.getDB().logDelta(callInfo, this);
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
   * Derive the delta.
   */
  private void deriveDelta() {
    // Try to minimize space use by matching equal rows soon.
    Iterator<Row> 
      a = snapshot.getRows().iterator(),
      b = state.getRows().iterator();
    boolean done = false;
    while (!done) {
      if (!a.hasNext()) {
        while (b.hasNext()) {
          update(b.next(), +1);
        }
        done = true;
      }
      else if (!b.hasNext()) {
        while (a.hasNext()) {
          update(a.next(), -1);
        }
        done = true;
      } 
      else {
        update(a.next(), -1);
        update(b.next(), +1);
      }
    }
  }

  /**
   * Update the delta
   * @param r Row.
   * @param d Increment.
   */
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
   * Get row iterator.
   * @param type Type of iterator.
   * @return An iterator for the specified type of data.
   */
  Iterator<Row> getIterator(IteratorType type) {
    Iterator<Row> res = null;
    switch(type) {
      case ACTUAL_NEW_DATA:
        res = new DeltaIterator(diff, false);
        break;
      case ACTUAL_OLD_DATA:
        res = new DeltaIterator(diff, true);
        break;
      case EXPECTED_NEW_DATA:
        break;
      case EXPECTED_OLD_DATA:
        break;
      default:
        throw new InternalAPIError();
    }
    return res;
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
    boolean iterateOldData;
    int count;

    DeltaIterator(LinkedHashMap<Row, Integer> diff, boolean oldData) {
      supportItr = diff.entrySet().iterator();
      iterateOldData = oldData;
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

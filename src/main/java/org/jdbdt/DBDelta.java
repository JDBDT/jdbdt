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
final class DBDelta {
  /**
   * Perform a delta verification.
   * 
   * <p>
   * Delta assertion methods in the {@link JDBDT} delegate the
   * actual verification to this method.
   * </p>
   * 
   * @param callInfo Call info.
   * @param oldData Old data expected.
   * @param newData New data expected.
   * @throws DBAssertionError If the verification fails.
   * @throws InvalidOperationException If the arguments are invalid.
   * 
   */
  static void verify(CallInfo callInfo,DataSet oldData, DataSet newData) {
    validateVerificationArguments(oldData, newData);
    final DataSource source = oldData.getSource();
    final DB db = source.getDB();
    final DataSet snapshot = source.getSnapshot();
    DataSet stateNow = source.executeQuery(callInfo, false);
    DBDelta delta = new DBDelta(snapshot, stateNow);
    db.logDelta(callInfo, delta);
    delta.before(oldData).after(newData).end();
  }
  
  @SuppressWarnings("javadoc")
  private static void
  validateVerificationArguments(DataSet oldData, DataSet newData) {
    if (oldData == null) {
      throw new InvalidOperationException("Null argument for 'old' data set.");
    }
    if (newData == null) {
      throw new InvalidOperationException("Null argument for 'new' data set.");
    }
    if (oldData.getSource() != newData.getSource()) {
      throw new InvalidOperationException("Data source mismatch between data sets.");
    }
    if (oldData.getSource().getSnapshot() == null) {
      throw new InvalidOperationException("Undefined snapshot for data source.");
    } 
  }
  
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
   * Representation of differences.
   */
  private final LinkedHashMap<Row, Integer> diff = new LinkedHashMap<>();

  /**
   * Construct a new delta.
   * @param preState Pre-state.
   * @param postState Post-state.
   */
  DBDelta(DataSet preState, DataSet postState) {
    source = preState.getSource();
    deriveDelta(preState, postState);
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
  public DBDelta before(Object... row) throws DBAssertionError {
    before(new Row(row));
    return this;
  }

  /**
   * Assert that the given data set is no longer defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case some row in the data set is still defined.         
   */
  public DBDelta before(DataSet data)  throws DBAssertionError {
    for (Row r : data.getRows()) {
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
    return this;
  }

  /**
   * Assert that the given data set is now defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DBAssertionError in case some row in the data set is still defined.    
   */
  public DBDelta after(DataSet data)  throws DBAssertionError {
    for (Row r : data.getRows()) {
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
   * @param pre Pre-state.
   * @param post Post-state.
   */
  private void deriveDelta(DataSet pre, DataSet post) {
    // Try to minimize space use by matching equal rows soon.
    Iterator<Row> 
      a = pre.getRows().iterator(),
      b = post.getRows().iterator();
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

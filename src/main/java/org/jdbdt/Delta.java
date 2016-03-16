package org.jdbdt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * Database delta, representing the incremental evolution 
 * of an observer's state.
 * 
 * <p>
 * A delta object is created with a call to {@link JDBDT#delta(DataSource)}. 
 * The delta will reflect the changes to database state
 * that the observer relates to (a table or a query).
 * These changes may be verified using a sequence of
 * calls on the delta object using {@link #after(Object...)}, 
 * {@code #before(Object...)},
 * followed by a final call to {@link #end()}.
 * </p>
 * 
 * @see JDBDT#delta(DataSource)
 * @since 0.1
 * 
 */
public final class Delta {
  /**
   * Observer instance. 
   */
  private final DataSource source;

  /**
   * Representation of differences.
   */
  private final LinkedHashMap<Row, Integer> diff;

  /**
   * Constructs a new database delta.
   * 
   * @param s Data source.
   */
  Delta(DataSource s) { 
    DataSet pre = s.getSnapshot(),
           post = s.executeQuery(false);
    source = s;
    diff = calcDiff(pre, post);
  }

  
  /**
   * Get meta-data.
   * @return the meta-data associated to this delta.
   */
  MetaData getMetaData() {
    return source.getMetaData();
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
   * @throws DeltaAssertionError in case the row is still defined.
   *          
   */
  public Delta before(Object... row) throws DeltaAssertionError {
    before(new RowImpl(row));
    return this;
  }

  /**
   * Assert that the given row is now defined.
   * 
   * @param row Column values for expected new row.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case the new row is not defined.
   */
  public Delta after(Object... row) throws DeltaAssertionError {
    after(new RowImpl(row));
    return this;
  }

  /**
   * Assert that the given data set is no longer defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case some row in the data set is still defined.         
   */
  public Delta before(DataSet data)  throws DeltaAssertionError {
    for (Row r : data) {
      before(r);
    }
    return this;
  }

  /**
   * Assert that the given data set is now defined.
   * 
   * @param data Data set.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case some row in the data set is still defined.    
   */
  public Delta after(DataSet data)  throws DeltaAssertionError {
    for (Row r : data) {
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
   * {@link DeltaAssertionError} will be thrown.
   * </p>
   * 
   * @throws DeltaAssertionError in case 
   *         there are remaining changes to verify
   */
  public void end() throws DeltaAssertionError {
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
   * Throw {@link DeltaAssertionError} and,
   * if logging is enabled, write delta to log.
   * 
   * @param msg Error message.
   * @throws DeltaAssertionError in any case (unconditionally).
   */
  private void throwDeltaAssertionError(String msg) {
    // TODO
//    Log errorLog = obs.getErrorLog();
//    if (errorLog != null) {
//      errorLog.write(this);
//    }
      throw new DeltaAssertionError(msg);
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
    Iterator<Row> a = rs1.iterator(),
                  b = rs2.iterator();
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

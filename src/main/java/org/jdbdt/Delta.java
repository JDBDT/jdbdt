package org.jdbdt;

/**
 * Database delta, representing the incremental evolution 
 * of an observer's state.
 * 
 * <p>
 * A delta object is derived from an {@link Snapshot} instance
 * with a call to {@link JDBDT#delta(Snapshot)}. 
 * The delta will reflect the changes to database state
 * that the observer relates to (a table or a query).
 * These changes may be verified using a sequence of
 * calls on the delta object using {@link #after(Object...)}, 
 * {@code #before(Object...)},
 * followed by a final call to {@link #end()}.
 * </p>
 * 
 * @see JDBDT#delta(Snapshot)
 * @see TypedDelta
 * @since 0.1
 * 
 */
public class Delta {
  /**
   * Observer instance. 
   */
  private final DataSource provider;

  /**
   * Rows no longer seen.
   */
  private final RowSet beforeSet;
  /**
   * Rows now seen.
   */
  private final RowSet afterSet;


  /**
   * Constructs a new database delta.
   * 
   * @param sp Spapshot provider.
   * @param oldDS Old data set.
   * @param newDS New data set.
   */
  Delta(DataSource sp, RowSet oldDS, RowSet newDS) {
    provider = sp;
    beforeSet = oldDS.diff(newDS);
    afterSet = newDS.diff(oldDS);
  }

  
  /**
   * Get meta-data.
   * @return the meta-data associated to this delta.
   */
  final MetaData getMetaData() {
    return provider.getMetaData();
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
  public final int size() {
    return beforeSet.size() + afterSet.size();
  }

  /**
   * Get 'before'-set.
   * @return The 'before' set of rows left to verify. 
   */
  final RowSet getBeforeSet() {
    return beforeSet;
  }

  /**
   * Get 'after'-set.
   * @return The 'after' set of rows left to verify. 
   */
  final RowSet getAfterSet() {
    return afterSet;
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
   * @param ds Data set.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case some row in the data set is still defined.         
   */
  public Delta before(DataSet ds)  throws DeltaAssertionError {
    for (Row r : ds) {
      before(r);
    }
    return this;
  }

  /**
   * Assert that the given data set is now defined.
   * 
   * @param ds Data set.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case some row in the data set is still defined.    
   */
  public Delta after(DataSet ds)  throws DeltaAssertionError {
    for (Row r : ds) {
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
  public final void end() throws DeltaAssertionError {
    int unverified = beforeSet.size() + afterSet.size();
    if (unverified > 0) {
      throwDeltaAssertionError(unverified + " unverified changes.");
    }
  }

  @SuppressWarnings("javadoc")
  private void after(Row r) {
    if (!afterSet.removeRow(r)) {
      throwDeltaAssertionError("New query result expected: "
          + r.toString());
    }
  }

  @SuppressWarnings("javadoc")
  private void before(Row r) {
    if (!beforeSet.removeRow(r)) {
      throwDeltaAssertionError("Old query result expected: "
          + r.toString());
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
}

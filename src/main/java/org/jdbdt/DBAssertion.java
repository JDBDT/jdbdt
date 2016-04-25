package org.jdbdt;

/**
 * Database delta.
 * 
 * @since 0.1
 * 
 */
final class DBAssertion {
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
  static void verify(CallInfo callInfo, DataSet oldData, DataSet newData) {
    validateVerificationArguments(oldData, newData);
    final DataSource source = oldData.getSource();
    //final DB db = source.getDB();
    final DataSet snapshot = source.getSnapshot();
    final DataSet stateNow = source.executeQuery(callInfo, false);
    final Delta dbDelta = new Delta(snapshot, stateNow);
    final Delta oldDataMatch 
      = new Delta(oldData.getRows().iterator(), dbDelta.deleted());
    final Delta newDataMatch 
      = new Delta(newData.getRows().iterator(), dbDelta.inserted());
    if (!oldDataMatch.isEmpty() || !newDataMatch.isEmpty()) {
      throw new DBAssertionError("Delta assertion failed");
    }   
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
}

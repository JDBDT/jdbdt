package org.jdbdt;

/**
 * Utility class with methods for assertion execution.
 * 
 * @since 0.1
 * 
 */
final class DBAssert {
  /**
   * Perform a delta assertion.
   * 
   * <p>
   * Delta assertion methods in the {@link JDBDT} delegate the
   * actual verification to this method.
   * </p>
   * 
   * @param callInfo Call info.
   * @param oldData Old data expected.
   * @param newData New data expected.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void deltaAssertion(CallInfo callInfo, DataSet oldData, DataSet newData) {
    validateDeltaAssertion(oldData, newData);
    final DataSource source = oldData.getSource();
    final DB db = source.getDB();
    final DataSet snapshot = source.getSnapshot();
    final DataSet stateNow = source.executeQuery(callInfo, false);
    final Delta dbDelta = new Delta(snapshot, stateNow);
    final Delta oldDataMatch 
      = new Delta(oldData.getRows().iterator(), dbDelta.deleted());
    final Delta newDataMatch 
      = new Delta(newData.getRows().iterator(), dbDelta.inserted());
    final DeltaAssertion da = 
      new DeltaAssertion(oldData, newData, oldDataMatch, newDataMatch);
    db.log(callInfo, da);
    if (!da.passed()) {
      throw new DBAssertionError(callInfo.getMessage());
    }
  }
  
  @SuppressWarnings("javadoc")
  private static void
  validateDeltaAssertion(DataSet oldData, DataSet newData) {
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
   * Perform a database state assertion.
   * 
   * @param callInfo Call info.
   * @param expected Expected data.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void stateAssertion(CallInfo callInfo, DataSet expected) {
    dataSetAssertion(callInfo, 
                     expected,   
                     expected.getSource().executeQuery(callInfo, false));
  }
  
  /**
   * Perform a data set assertion.
   * 
   * 
   * @param callInfo Call info.
   * @param expected Expected data.
   * @param actual Actual data.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void dataSetAssertion(CallInfo callInfo, DataSet expected, DataSet actual) {
    validateDataSetAssertion(expected, actual);
    final Delta delta = new Delta(expected, actual); 
    final DataSetAssertion assertion = 
      new DataSetAssertion(expected, delta);
    expected.getSource().getDB().log(callInfo, assertion);
    if (! assertion.passed()) {
      throw new DBAssertionError(callInfo.getMessage());
    }
  }

  @SuppressWarnings("javadoc")
  private static void
  validateDataSetAssertion(DataSet expected, DataSet actual) {
    if (expected == null) {
      throw new InvalidOperationException("Null argument for 'old' data set.");
    }
    if (actual == null) {
      throw new InvalidOperationException("Null argument for 'new' data set.");
    }
    if (expected.getSource() != actual.getSource()) {
      throw new InvalidOperationException("Data source mismatch between data sets.");
    }
  }
  
  /**
   * Private constructor to prevent instantiation.
   */
  private DBAssert() {
    
  }
}

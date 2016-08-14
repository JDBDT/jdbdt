package org.jdbdt;

import java.util.Iterator;

/**
 * Data set assertion data.
 * 
 * @since 0.1
 */
final class DataSetAssertion {

  /**
   * Iterator type enumeration.
   */
  enum IteratorType {
    /** Expected (in full). */
    EXPECTED_DATA,
    /** Expected data not matched. */
    ERRORS_EXPECTED,
    /**  Actual data not matched. */
    ERRORS_ACTUAL
  }
  
  /**
   * 'Old' data expected.
   */
  private final DataSet data;
 
  /**
   * Delta for actual match.
   */
  private final Delta delta;
  
  
  /**
   * Constructor.
   * @param data Expected data
   * @param delta Actual delta (empty if assertion passes)
   */
  DataSetAssertion(DataSet data, Delta delta) {
    this.data = data;
    this.delta = delta;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return delta.isEmpty();
  }

  /**
   * Get information for the assertion.
   * @param type Type of iterator
   * @return An iterator for the specified data.
   */
  Iterator<Row> data(IteratorType type) {
    Iterator<Row> itr;
    switch(type) {
      case EXPECTED_DATA:
        itr = data.getRows().iterator();
        break;
      case ERRORS_EXPECTED:
        itr = delta.deleted();
        break;
      case ERRORS_ACTUAL:
        itr = delta.inserted();
        break;
      default:
        throw new InternalAPIError("Unexpected case!");      
    }
    return itr;
  }

  /**
   * Get data source.
   * @return Data source associated to state assertion.
   */
  public DataSource getSource() {
    return data.getSource();
  }
  
}

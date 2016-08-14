package org.jdbdt;

import java.util.Iterator;

/**
 * Delta assertion.
 * 
 * @since 0.1
 */
final class DeltaAssertion {

  /**
   * Iterator type enumeration.
   */
  enum IteratorType {
    /** 'Old' data expected (in full). */
    OLD_DATA_EXPECTED,
    /** 'New' data expected (in full). */
    NEW_DATA_EXPECTED,
    /** Expected 'old' data not matched. */
    OLD_DATA_ERRORS_EXPECTED,
    /**  Actual 'old' data not matched. */
    OLD_DATA_ERRORS_ACTUAL,
    /**  Expected 'new' data not matched. */
    NEW_DATA_ERRORS_EXPECTED,
    /**  Actual 'new' data not matched. */
    NEW_DATA_ERRORS_ACTUAL
  }
  
  /**
   * 'Old' data expected.
   */
  private final DataSet oldData;

  /**
   * 'New' data expected.
   */
  private final DataSet newData;
  
  /**
   * Delta for actual match of 'old' data.
   */
  private final Delta oldDataMatch;
  
  /**
   * Delta for actual match of 'new' data.
   */
  private final Delta newDataMatch;
  
  /**
   * Constructor.
   * @param oldData 'old' data expected
   * @param newData 'new' data expected
   * @param oldDataMatch 'old' data delta
   * @param newDataMatch 'new' data delta
   */
  DeltaAssertion(DataSet oldData, DataSet newData, Delta oldDataMatch,
      Delta newDataMatch) {
    this.oldData = oldData;
    this.newData = newData;
    this.oldDataMatch = oldDataMatch;
    this.newDataMatch = newDataMatch;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return oldDataMatch.isEmpty() && newDataMatch.isEmpty();
  }

  /**
   * Get information for the assertion.
   * @param type Type of iterator
   * @return An iterator for the specified data.
   */
  Iterator<Row> data(IteratorType type) {
    Iterator<Row> itr;
    switch(type) {
      case OLD_DATA_EXPECTED:
        itr = oldData.getRows().iterator();
        break;
      case NEW_DATA_EXPECTED:
        itr = newData.getRows().iterator();
        break;
      case OLD_DATA_ERRORS_EXPECTED:
        itr = oldDataMatch.deleted();
        break;
      case OLD_DATA_ERRORS_ACTUAL:
        itr = oldDataMatch.inserted();
        break;
      case NEW_DATA_ERRORS_EXPECTED:
        itr = newDataMatch.deleted();
        break;
      case NEW_DATA_ERRORS_ACTUAL:
        itr = newDataMatch.inserted();
        break;
      default:
        throw new InternalAPIError("Unexpected case!");      
    }
    return itr;
  }

  /**
   * Get data source.
   * @return Data source associate to the assertion.
   */
  DataSource getSource() {
    return oldData.getSource();
  }
}

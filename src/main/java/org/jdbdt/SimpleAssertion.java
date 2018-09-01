package org.jdbdt;

/**
 * Simple assertion.
 * 
 * @since 1.0
 */
final class SimpleAssertion {
  /**
   * Data source this assertion relates to.
   */
  private final DataSource dataSource;
  
  /**
   * Expected data.
   */
  private final Object expected;
 
  /**
   * Actual data.
   */
  private final Object actual;
  
  
  /**
   * Constructor.
   * @param dataSource Data source.
   * @param expected Expected data.
   * @param actual Actual data.
   */
  SimpleAssertion(DataSource dataSource, Object expected, Object actual) {
    this.dataSource = dataSource;
    this.expected = expected;
    this.actual = actual;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return expected.equals(actual);
  }

  
  /**
   * Get data source.
   * @return Data source associated to state assertion.
   */
  DataSource getSource() {
    return dataSource;
  }
  
  /**
   * Get expected result.
   * @return The expected result.
   */
  Object getExpectedResult() {
    return expected;
    
  }
  
  /**
   * Get expected result.
   * @return The expected result.
   */
  Object getActualResult() {
    return actual;
  }
}

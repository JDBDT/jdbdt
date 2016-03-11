package org.jdbdt;

/**
 * Error thrown due to a failed delta assertion.
 * 
 * <p>
 * Exceptions of this type are thrown when a delta assertion fails.
 * </p>
 * 
 * @see JDBDT#delta(DataSource)
 * @see JDBDT#delta(TypedSnapshot)
 * @see Delta
 * @see TypedDelta
 * 
 * @since 0.1
 */
public final class DeltaAssertionError extends JDBDTAssertionError {

  /**
   * Constructs exception with given error message.
   * @param message Error message.
   */
  DeltaAssertionError(String message) {
    super(message);
  }
  
  @SuppressWarnings("javadoc")
  private static final long serialVersionUID = 1L;

}

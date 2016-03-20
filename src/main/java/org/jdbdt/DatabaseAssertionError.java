package org.jdbdt;

/**
 * Error thrown due to a failed database assertion.
 * 
 * @since 0.1
 */
public final class DatabaseAssertionError extends AssertionError {

  /**
   * Constructs exception with given error message.
   * @param message Error message.
   */
  public DatabaseAssertionError(String message) {
    super(message);
  }
  
  @SuppressWarnings("javadoc")
  private static final long serialVersionUID = 1L;

}

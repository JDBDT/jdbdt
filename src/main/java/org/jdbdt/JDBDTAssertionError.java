package org.jdbdt;

/**
 * JDBDT assertion error.
 * 
 * <p>
 * Exceptions of this type are thrown when a JDBDT assertion fails.
 * </p>
 * 
 * @since 0.1
 */
public class JDBDTAssertionError extends AssertionError {

  /**
   * Constructs exception with given error message.
   * @param message Error message.
   */
  JDBDTAssertionError(String message) {
    super(message);
  }

  @SuppressWarnings("javadoc")
  private static final long serialVersionUID = 1L;
}

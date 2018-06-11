package org.jdbdt;

/**
 * Exception thrown when there is an error
 * evaluating a column filler.
 *  
 * @see ColumnFiller
 * @see DataSetBuilder#generate(int)
 *  
 * @since 0.2
 */
public final class ColumnFillerException extends JDBDTRuntimeException {
  /**
   * Constructs a new exception using supplied message.
   * and associated cause.
   * @param message Error message.
   */
  public ColumnFillerException(String message) {
    super(message);
  }
  
  /**
   * Constructs a new exception using supplied message
   * and associated cause.
   * @param message Error message.
   * @param cause Exception thrown during column filler execution.
   */
  public ColumnFillerException(String message, Exception cause) {
    super(message, cause);
  }

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
  
}

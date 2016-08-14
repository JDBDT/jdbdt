package org.jdbdt;

/**
 * Exception thrown when there is an error
 * evaluating a column filler.
 *  
 * @see ColumnFiller
 * @see DataSetBuilder
 *  
 * @since 0.1
 */
public final class ColumnFillerException extends RuntimeException {
  
  /**
   * Constructs a new exception using supplied message
   * and associated cause.
   * @param msg Error message.
   * @param cause Exception thrown during column filler execution.
   */
  public ColumnFillerException(String msg, Exception cause) {
    super(msg, cause);
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
  
}

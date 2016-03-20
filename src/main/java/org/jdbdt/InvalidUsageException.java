package org.jdbdt;

/**
 * Exception thrown due to an invalid use of the JDBDT API.
 * 
 * @since 0.1
 */
public final class InvalidUsageException extends JDBDTRuntimeException {

  /**
   * Constructs a new exception using supplied message.
   * @param msg Exception message.
   */
  public InvalidUsageException(String msg) {
    super(msg);
  }
  
  /**
   * Constructs a new exception using supplied message
   * and associated cause.
   * @param msg Exception message.
   * @param cause Cause for exception.
   */
  public InvalidUsageException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
  
 
}

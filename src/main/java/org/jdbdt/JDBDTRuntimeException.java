package org.jdbdt;

/**
 * Base class for JDBDT runtime exceptions.
 * 
 * @since 1.0
 */
public class JDBDTRuntimeException extends RuntimeException {
  /**
   * Constructs a new exception using supplied message.
   * @param msg Exception message.
   */
  public JDBDTRuntimeException(String msg) {
    super(msg);
  }
  
  /**
   * Constructs a new exception using supplied message
   * and associated cause.
   * @param msg Exception message.
   * @param cause Cause for exception.
   */
  public JDBDTRuntimeException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}

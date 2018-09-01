package org.jdbdt;

/**
 * Exception thrown due to an unsupported operation.
 * 
 * @since 1.0
 */
public final class UnsupportedOperationException extends JDBDTRuntimeException {
  /**
   * Constructs a new exception using supplied message.
   * @param msg Exception message.
   */
  public UnsupportedOperationException(String msg) {
    super(msg);
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L; 
}

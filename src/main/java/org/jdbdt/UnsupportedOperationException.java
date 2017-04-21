package org.jdbdt;

/**
 * Exception thrown due to an unsupported operation.
 * 
 * @since 0.9
 */
public final class UnsupportedOperationException extends RuntimeException {

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

package org.jdbdt;


/**
 * Exception thrown by JDBDT due to an unexpected internal error.
 * 
 * <p>
 * Exceptions of this kind should never happen in principle.
 * If they do, there is either likely a bug in JDBDT or 
 * abnormal conditions in the JVM environment.
 * </p>
 * 
 * @since 0.9
 */
public final class InternalErrorException extends JDBDTRuntimeException {

  /**
   * Constructs exception with empty message.
   */
  public InternalErrorException() {
    super("Internal error!");
  }
  
  /**
   * Constructs exception using supplied message.
   * @param message Error message. 
   */
  public InternalErrorException(String message) {
    super(message);
  }
  
  /**
   * Constructs exception with given cause.
   * @param cause Cause for the exception.
   */
  public InternalErrorException(Throwable cause) {
    super("Unexpected internal error", cause);
  }
  
 

  

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}

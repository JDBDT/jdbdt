package org.jdbdt;


/**
 * Exception thrown by JDBDT due to an unexpected internal error.
 * 
 * <p>
 * Exceptions of this kind should never happen in principle.
 * If they do, there is either likely a bug in JDBDT or something
 * abnormal with the environment setup.
 * </p>
 * 
 * @since 0.1
 */
final class InternalAPIError extends Error {

  /**
   * Constructs exception with empty message.
   */
  public InternalAPIError() {
    
  }
  /**
   * Constructs exception with given cause.
   * @param cause Cause for the exception.
   */
  public InternalAPIError(Throwable cause) {
    super("Unexpected internal error", cause);
  }
  
  /**
   * Constructs exception using supplied message.
   * @param message Error message. 
   */
  public InternalAPIError(String message) {
    super(message);
  }



  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}

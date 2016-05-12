package org.jdbdt;

import java.sql.SQLException;

/**
 * Exception thrown due to database errors.
 * 
 * <p>
 * An exception of this type wraps a {@link SQLException}
 * (a checked exception type).
 * </p>
 * 
 * @since 0.1
 */
public final class DBExecutionException extends RuntimeException {

  /**
   * Constructs the exception with given cause.
   * @param cause Cause for the exception.
   */
  public DBExecutionException(SQLException cause) {
    super("Database execution error", cause);
  }
  
  /**
   * Get SQL exception that cause this exception.
   * @return The SQL exception at stake.
   */
  @Override
  public SQLException getCause() {
    return (SQLException) super.getCause();
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}

package org.jdbdt;

import java.sql.SQLException;

/**
 * Exception thrown due to a database error.
 * 
 * <p>
 * An exception of this type wraps an instance of 
 * {@link SQLException} (a checked exception type)
 * that occurred while executing a database operation
 * issued by JDBDT. 
 * </p>
 * 
 * @since 0.1
 */
public final class DBExecutionException extends JDBDTRuntimeException {

  /**
   * Constructs the exception with given cause.
   * @param cause Cause for the exception.
   */
  public DBExecutionException(SQLException cause) {
    super("Database execution error", cause);
  }
  
  /**
   * Get cause of the exception. 
   * 
   * @return An instance of {@link java.sql.SQLException}.n 
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

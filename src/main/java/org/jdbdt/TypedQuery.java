package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Interface for typed queries.
 * 
 * @param <T> Type of objects for the query.
 * 
 * @since 0.1
 */
public interface TypedQuery<T> {
  /**
   * Get conversion function
   * @return The conversion function associated to typed query.
   */
  Conversion<T> conversion(); 
  
  /**
   * Get SQL statement associated to this query.
   * @return Prepared statement associated to the query.
   * @throws SQLException if a database error occurs.
   */
  PreparedStatement getStatement() throws SQLException;
  
}

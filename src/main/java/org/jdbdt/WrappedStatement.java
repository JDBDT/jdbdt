package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Statement wrapper.
 *
 * @since 0.6
 */
final class WrappedStatement implements AutoCloseable {

  /**
   * Wrapped statement.
   */
  private final PreparedStatement statement;
  /**
   * Reuse flag.
   */
  private final boolean reuse;
  
  /**
   * Constructor.
   * @param statement Statement.
   * @param reuse Flag indicating if 
   */
  WrappedStatement(PreparedStatement statement, boolean reuse) {
    this.statement = statement;
    this.reuse = reuse;
  }
  
  /**
   * Get wrapped statement.
   * @return The prepared statement.
   */
  PreparedStatement getStatement() {
    return statement;
  }
  
  @Override
  public void close() throws SQLException  {
    if (reuse) {
      statement.clearParameters();
    } else {
      statement.close();
    }
  }

}

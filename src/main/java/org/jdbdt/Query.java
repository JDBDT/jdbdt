package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract parent class for database queries.
 * 
 * <p>
 * Concrete instance of this type, ({@link TableQuery} and {@link TypedTableQuery})
 * are created respectively through {@link JDBDT#selectFrom(Table)}
 * and {@link JDBDT#selectFrom(TypedTable)}. 
 * </p>
 * 
 * @see TableQuery
 * @see TypedTableQuery
 * 
 * @since 0.1
 *
 */
public abstract class Query {

  /**
   * Compiled statement.
   */
  private PreparedStatement compiledStmt;
  
  /**
   * Constructs an a non-compiled query.
   * This constructor is for use by subclasses.
   */
  Query() {
    compiledStmt = null;
  }
  
  /**
   * Constructs a compiled query.
   * @param stmt Compiled statement.
   */
  Query(PreparedStatement stmt) {
    compiledStmt = stmt;
  }
  
  /**
   * Get compiled statement for this query.
   * 
   * <p>Subclasses should override this method.
   * In conjunction, subclasses should use 
   * {@link #compile(Connection, String)}
   * to set up the compiled statement for the query.
   * </p>
   * @return Compiled statement for the query.
   */
  PreparedStatement getStatement() {
    checkCompiled();
    return compiledStmt;
  }
  
  /**
   * Test if query is compiled.
   * @return <code>true</code> if query is compiled,
   *   <code>false</code> otherwise.
   */
  final boolean isCompiled() {
    return compiledStmt != null;
  }
  
  /**
   * Validate that query has not been compiled yet.
   * @throws InvalidUsageException if query has been already compiled.
   */
  final void checkNotCompiled() throws InvalidUsageException {
    if (compiledStmt != null) {
      throw new InvalidUsageException("Query has already been compiled.");
    }
  }
  
  /**
   * Validate that query has already been compiled.
   * @throws InvalidUsageException if query has been already compiled.
   */
  final void checkCompiled() throws InvalidUsageException {
    if (compiledStmt == null) {
      throw new InvalidUsageException("Query has not yet been compiled.");
    }
  }
  
  /**
   * Compile query for given database and SQL code.
   * This method should be used by subclasses.
   * @param conn Database connection.
   * @param sql SQL for the query.
   * @throws UnexpectedDatabaseException if a database error occurs compiling query.
   * @throws InvalidUsageException if the query is already compiled.
   * 
   */
  final void compile(Connection conn, String sql) 
  throws InvalidUsageException, UnexpectedDatabaseException  {
    checkNotCompiled();
    try { 
      compiledStmt = StatementPool.compile(conn, sql);
    }
    catch (SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
  }
}

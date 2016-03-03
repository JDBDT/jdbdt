package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Base class for database queries.
 * 
 * <p>
 * Instances of this type are created as follows:
 * <ul>
 * <li>
 * Table queries in untyped or typed form, ({@link TableQuery} and {@link TypedTableQuery})
 * are created respectively through {@link JDBDT#selectFrom(Table)}
 * and {@link JDBDT#selectFrom(TypedTable)}. 
 * In principle, table queries should be suitable for most cases.
 * </li>
 * <li>
 * Direct instances of this class can be created
 * from custom SQL statements through {@link JDBDT#selectUsing(PreparedStatement)},
 * as shown in the example below.
 * </li>
 * <li> 
 * A {@link TypedCustomQuery} for custom SQL statements is created through 
 * {@link JDBDT#selectUsing(PreparedStatement,Conversion)}.
 * </li>
 * </ul>
 * 
 * <p><b>Example - creation of untyped query from custom SQL statements</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Query;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * String customSql = ...;
 * Query q = selectUsing(conn.prepareStament(customSql));
 * </pre></blockquote>
 * <p>For examples of "standard" table queries, check the documentation
 * of {@link TableQuery} and {@link TypedTableQuery}.
 * </p>
 * 
 * 
 * @see TableQuery
 * @see TypedTableQuery
 * @see TypedCustomQuery
 * 
 * @since 0.1
 *
 */
public class Query {

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
  public PreparedStatement getStatement() {
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

package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

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
   * Meta-data.
   */
  private MetaData metaData;

  /**
   * Last snapshot.
   */
  private RowSet snapshot; 
   
  /**
   * Constructs a non-compiled query.
   * This constructor is for use by subclasses.
   */
  Query() {
    this(null, null);
  }
  
  /**
   * Constructs query.
   * @param stmt Compiled statement.
   * @param md Meta-data for statement.
   */
  Query(PreparedStatement stmt, MetaData md) {
    compiledStmt = stmt;
    metaData = md;
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
   * Get meta-data for this query.
   *
   * @return Meta-data instance associated to query.
   */
  final MetaData getMetaData() {
    checkCompiled();
    return metaData;
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
      metaData = new MetaData(compiledStmt);
    }
    catch (SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
  }

  /**
   * Take snapshot.
   */
  final void takeSnapshot() {
    checkCompiled();
    RowSet rs = new RowSet();
    Query.executeQuery(compiledStmt, metaData, null, r -> rs.addRow(r));
    snapshot = rs;
  }
  
  /**
   * Get last snapshot.
   * @return Last snapshot taken.
   */
  final RowSet getSnapshot() {
    if (snapshot == null) {
      throw new InvalidUsageException("No snapshot taken for query.");
    }
    return snapshot;
  }
  
  /**
   * Execute query.
   * @param queryStmt Query statement.
   * @param md Meta data.
   * @param queryArgs Query arguments.
   * @param c Row consumer.
   */
  static void executeQuery(PreparedStatement queryStmt, 
                           MetaData md, 
                           Object[] queryArgs, 
                           Consumer<Row> c) {
    try { 
      if (queryArgs != null && queryArgs.length > 0) {
        for (int i=0; i < queryArgs.length; i++) {
          queryStmt.setObject(i + 1, queryArgs[i]);
        }
      }
      ResultSet rs = queryStmt.executeQuery();
      try {
        int colCount = md.getColumnCount();
        while (rs.next()) {
          Object[] data = new Object[colCount];
          for (int i = 0; i < colCount; i++) {          
            data[i] = rs.getObject(i+1);
          }
          c.accept(new RowImpl(data));
        }
      }
      finally {
        rs.close();
      }
    } 
    catch(SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
    
  }
}

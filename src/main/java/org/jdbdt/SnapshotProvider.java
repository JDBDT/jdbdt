package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract class for snapshot providers 
 * 
 * @since 0.1
 *
 */
public abstract class SnapshotProvider {

  /**
   * Selection query for the table.
   */
  private PreparedStatement queryStmt;

  /**
   * Meta-data for query statement.
   */
  private MetaData metaData;
  
  /**
   * Last snapshot (if any).
   */
  private RowSet snapshot;
  
  /**
   * Constructor.
   */
  SnapshotProvider() {
    queryStmt = null;
    metaData = null;
    snapshot = null;
  }
  
  /**
   * Get query.
   * @return The query statement for the snapshot provider.
   */
  PreparedStatement getQuery() {
    checkCompiled();
    return queryStmt;
  }
  /**
   * Get meta-data.
   * 
   * @return The meta-data instance for the snaphot provider.
   */
  final MetaData getMetaData() {
    checkCompiled();
    return metaData;
  }
  
  /**
   * Validate that query has not been compiled yet.
   * @throws InvalidUsageException if query has been already compiled.
   */
  final void checkNotCompiled() throws InvalidUsageException {
    if (queryStmt != null) {
      throw new InvalidUsageException("Query has already been compiled.");
    }
  }
  
  /**
   * Validate that query has already been compiled.
   * @throws InvalidUsageException if query has been already compiled.
   */
  final void checkCompiled() throws InvalidUsageException {
    if (queryStmt == null) {
      throw new InvalidUsageException("Query has not yet been compiled.");
    }
  }
  
  /**
   * Compile query.
   * This method should be used by subclasses.
   * @throws UnexpectedDatabaseException if a database error occurs compiling query.
   * @throws InvalidUsageException if the query is already compiled.
   * 
   */
  final void compileQuery() 
  throws InvalidUsageException, UnexpectedDatabaseException  {
    checkNotCompiled();
    try {
      queryStmt = StatementPool.compile(getConnection(), getSQLForQuery());
      metaData = new MetaData(queryStmt);
    }
    catch (SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
  }
  

  /**
   * Get database connection.
   * @return The database connection associate to this snapshot provider.
   */
  abstract Connection getConnection(); 
  
  /**
   * Get SQL code for query.
   * @return SQL code for the database query.
   */
  abstract String getSQLForQuery();
  
  /**
   * Execute query.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final RowSet executeQuery(boolean takeSnapshot) {
    checkCompiled();
    RowSet rs = new RowSet();
    Query.executeQuery(queryStmt, metaData, null, r -> rs.addRow(r));
    if (takeSnapshot) {
      setSnapshot(rs);
    }
    return rs;
  }
  
  /**
   * Get last snapshot.
   * @return Last snapshot taken.
   */
  final RowSet getSnapshot() {
    if (snapshot == null) {
      throw new InvalidUsageException("No snapshot taken for table.");
    }
    return snapshot;
  }
  
  /**
   * Set snapshot data.
   * @param s Data set to assume as snapshot.
   */
  final void setSnapshot(RowSet s) {
    if (snapshot != null) {
      snapshot.clear();
    }
    snapshot = s;
  }
  
}

package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Abstract class for snapshot providers 
 * 
 * @since 0.1
 *
 */
public abstract class DataSource {

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
  private DataSet snapshot;
  
  /**
   * Constructor.
   */
  DataSource() {
    queryStmt = null;
    metaData = null;
    snapshot = null;
  }
  
  /**
   * Get query.
   * @return The query statement for the snapshot provider.
   */
  PreparedStatement getQueryStatement() {
    if (queryStmt == null) {
      compileQuery();
    }
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
   * Get arguments for issuing query.
   * @return Arguments to use for query, <code>null</code> otherwise.
   */
  abstract Object[] getQueryArguments();
 
  /**
   * Execute query.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final DataSet executeQuery(boolean takeSnapshot) {

    DataSet rs = new DataSet();
    executeQuery(getQueryStatement(), getMetaData(), getQueryArguments(), r -> rs.addRow(r));
    if (takeSnapshot) {
      setSnapshot(rs);
    }
    return rs;
  }
  
  /**
   * Get last snapshot.
   * @return Last snapshot taken.
   */
  final DataSet getSnapshot() {
    if (snapshot == null) {
      throw new InvalidUsageException("No snapshot taken for table.");
    }
    return snapshot;
  }
  
  /**
   * Set snapshot data.
   * @param s Data set to assume as snapshot.
   */
  final void setSnapshot(DataSet s) {
    if (snapshot != null) {
      snapshot.clear();
    }
    snapshot = s;
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

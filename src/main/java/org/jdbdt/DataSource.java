package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Base class for data sources.
 * 
 * @since 0.1
 * 
 */
public abstract class DataSource {

  /**
   * Database instance for this data source.
   */
  private final DB db;
  
  /**
   * Query statement.
   */
  private PreparedStatement queryStmt = null;

  /**
   * Meta-data for query statement.
   */
  private MetaData metaData = null;

  /**
   * Last snapshot (if any).
   */
  private DataSet snapshot = null;

  /**
   * Constructor.
   * @param db Database instance.
   */
  DataSource(DB db) {
    this.db = db;
  }

  /**
   * Get database instance.
   * @return Database instance associated to this data source.
   */
  public final DB getDB() {
    return db;
  }
  
  /**
   * Get column count.
   * @return Column count.
   */
  final int getColumnCount() {
    ensureCompiled();
    return getColumns().length;
  }

  /**
   * Get query.
   * @return The query statement for the data source.
   */
  PreparedStatement getQueryStatement() {
    ensureCompiled();
    return queryStmt;
  }

  /**
   * Get meta-data.
   * 
   * @return Meta-data for the data source query.
   */
  final MetaData getMetaData() {
    ensureCompiled();
    return metaData;
  }

  /**
   * Ensure query is compiled.
   */
  void ensureCompiled() {
    if (queryStmt == null) {
      try {
        PreparedStatement stmt = db.compile(getSQLForQuery());
        MetaData md = new MetaData(stmt);
        if (getColumns() == null) {
          String[] cols = new String[md.getColumnCount()];
          for (int i = 0; i < cols.length; i++) {
            cols[i] = md.getLabel(i);
          }
          columns(cols);
        }
        queryStmt = stmt;
        metaData = md;
      }
      catch (SQLException e) {
        throw new UnexpectedDatabaseException(e);
      }
    }
  }
  /**
   * Validate that query has not been compiled yet.
   * @throws InvalidOperationException if query has been already compiled.
   */
  final void checkNotCompiled() throws InvalidOperationException {
    if (queryStmt != null) {
      throw new InvalidOperationException("Query has already been compiled.");
    }
  }

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
   * Set column names.
   * @param columns SQL columns.
   * @return Implementations should return <code>this</code> for chained calls.
   */
  public abstract DataSource columns(String... columns);
    
  /** 
   * Get column names.
   * @return Array of column names.
   */
  abstract String[] getColumns();

  /**
   * Execute query.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final DataSet executeQuery(boolean takeSnapshot) {
    DataSet data = new DataSet(this, true);
    executeQuery(getQueryStatement(), getMetaData(), getQueryArguments(), r -> data.addRow(r));
    if (takeSnapshot) {
      setSnapshot(data);
      getDB().logSnapshot(data);
    }
    return data;
  }

  /**
   * Get last snapshot.
   * @return Last snapshot taken.
   */
  final DataSet getSnapshot() {
    if (snapshot == null) {
      throw new InvalidOperationException("No snapshot taken!");
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

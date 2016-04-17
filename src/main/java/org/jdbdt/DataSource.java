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
   * Column names.
   */
  private String[] columns;
  
  /**
   * Query arguments (if any).
   */
  private Object[] queryArgs;
  
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
   * Set columns for data source.
   * 
   * @param columns Column names.
   */
  final void setColumns(String[] columns) {
    if (this.columns != null) {
      throw new InvalidOperationException("Columns are already set.");
    }
    this.columns = columns.clone();    
  }
 
  /** 
   * Get column names.
   * @return Array of column names.
   */
  final String[] getColumns() {
    return columns;
  }
  
  /**
   * Get column count.
   * @return Column count.
   */
  final int getColumnCount() {
    ensureCompiled();
    return columns.length;
  }

  /**
   * Get query.
   * @return The query statement for the data source.
   */
  final PreparedStatement getQueryStatement() {
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
   * Set arguments for issuing query.
   * @param args Arguments to use for query.
   */
  final void setQueryArguments(Object[] args) {
    if (queryArgs != null) {
      throw new InvalidOperationException("Query arguments are already set.");
    }
    if (args == null || args.length == 0) {
      throw new InvalidOperationException("Invalid query arguments (null / empty array?).");
    }
    queryArgs = args.clone();
  }

  /**
   * Get query arguments.
   * @return Array of arguments if any, otherwise <code>null</code>.
   */
  final Object[] getQueryArguments() {
    return queryArgs;
  }
  
  /**
   * Ensure query is compiled.
   */
  private void ensureCompiled() {
    if (queryStmt == null) {
      try {
        PreparedStatement stmt = db.compile(getSQLForQuery());
        MetaData md = new MetaData(stmt);
        if (getColumns() == null) {
          String[] cols = new String[md.getColumnCount()];
          for (int i = 0; i < cols.length; i++) {
            cols[i] = md.getLabel(i);
          }
          setColumns(cols);
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
   * Execute query.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final DataSet executeQuery(boolean takeSnapshot) {
    DataSet data = new DataSet(this);
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
    s.setReadOnly();
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
          c.accept(new Row(data));
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

package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
  private String[] columns = null;

  /**
   * Query arguments (if any).
   */
  private Object[] queryArgs = null;

  /**
   * Last snapshot (if any).
   */
  private DataSet snapshot = null;

  /**
   * The empty data set, as returned by {@link JDBDT#empty(DataSource)}
   * (computed lazily).
   */
  private DataSet theEmptyOne = null;

  /**
   * Constructor.
   * @param db Database instance.
   * @param queryArgs Optional arguments for database query.
   */
  DataSource(DB db, Object... queryArgs) {
    this.db = db;
    this.queryArgs = queryArgs;
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
  public final String[] getColumns() {
    return columns;
  }

  /**
   * Get column count.
   * @return Column count.
   */
  public final int getColumnCount() {
    ensureCompiled();
    return columns.length;
  }

  /**
   * Array list shared by all data set instances returned 
   * by {@link #theEmptySet()}.
   */
  private static final ArrayList<Row> EMPTY_ROW_LIST = new ArrayList<>();

  /**
   * Return an empty, read-only data set,
   * for use by {@link JDBDT#empty(DataSource)}
   * @return Empty, read-only data set.
   */
  final DataSet theEmptySet() {
    if (theEmptyOne == null) {
      theEmptyOne = new DataSet(this, EMPTY_ROW_LIST);
      theEmptyOne.setReadOnly();
    }
    return theEmptyOne;
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
        throw new DBExecutionException(e);
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
  public abstract String getSQLForQuery();

  /**
   * Execute query.
   * @param callInfo Call info.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final DataSet executeQuery(CallInfo callInfo, boolean takeSnapshot) {
    DataSet data = new DataSet(this);
    executeQuery(getQueryStatement(), getMetaData(), getQueryArguments(), r -> data.addRow(r));
    if (takeSnapshot) {
      setSnapshot(data);
      getDB().logSnapshot(callInfo, data);
    } else {
      getDB().logQuery(callInfo, data);
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
      throw new DBExecutionException(e);
    } 
  }


}

/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for data sources.
 * 
 * @since 1.0
 * 
 */
public abstract class DataSource {

  /**
   * Database instance for this data source.
   */
  private final DB db;

  /**
   * Meta-data for query statement.
   */
  private MetaData metaData = null;

  /**
   * Columns.
   */
  private List<String> columns;

  /**
   * SQL code for query.
   */
  private final String querySQL;
  
  /**
   * Query arguments (if any).
   */
  private final Object[] queryArgs;

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
   * Dirty status flag.
   */
  private boolean dirty;

  /**
   * Constructor.
   * @param callInfo Call info.
   * @param db Database instance.
   * @param sql SQL code for query.
   * @param queryArgs Optional arguments for database query.
   */
  protected DataSource(CallInfo callInfo, DB db, String sql, Object... queryArgs) {
    this.db = db;
    this.querySQL = sql;
    this.queryArgs = queryArgs;
    this.dirty = true;
    db.access(callInfo, () -> {
      try (WrappedStatement stmt = db.compile(querySQL)) {
        MetaData md = new MetaData(stmt.getStatement());
        String[] cols = new String[md.getColumnCount()];
        boolean caseSensitive = db.isEnabled(DB.Option.CASE_SENSITIVE_COLUMN_NAMES);
        for (int i = 0; i < cols.length; i++) {
          String cname = md.getLabel(i);
          String cnameUC = cname.toUpperCase();
          if (caseSensitive && !cname.equals(cnameUC)) {
            cname = '\"' + cname +  '\"';
          } else {
            cname = cnameUC;
          }
          cols[i] = cname;
        }
        columns = Collections.unmodifiableList(Arrays.asList(cols));
        metaData = md;
        return 0;
      }
    });
  }
  /**
   * Constructor with supplied columns (testing purposes only).
   * @param db Database handle.
   * @param columns Columns.
   */
  DataSource(DB db, String[] columns) {
    this.db = db;
    this.querySQL = null;
    this.queryArgs = null;
    this.dirty = true;
    this.metaData = null;
    ArrayList<String> list = new ArrayList<>();
    for (String c : columns) {
      if (!db.isEnabled(DB.Option.CASE_SENSITIVE_COLUMN_NAMES)) {
        c = c.toUpperCase();
      }
      list.add(c);
    }
    this.columns = Collections.unmodifiableList(list);
  }

  /**
   * Get database instance.
   * @return Database instance associated to this data source.
   */
  public final DB getDB() {
    return db;
  }

  /** 
   * Get columns.
   * 
   * <p>Note: the returned list is unmodifiable.</p>
   * 
   * @return The list of columns for the data source.
   */
  public final List<String> getColumns() {
    return columns;
  }

  /**
   * Get column count.
   * @return Column count.
   */
  public final int getColumnCount() {
    return columns.size();
  }

  /**
   * Get column name.
   * @param index Column index between <code>0</code> and <code>getColumnCount() - 1</code>
   * @return Name of column.
   */
  public final String getColumnName(int index) {
    if (index < 0 || index >= columns.size()) {
      throw new InvalidOperationException("Invalid column index: " + index);
    }
    return columns.get(index);
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
   * Get meta-data.
   * @return Meta-data for the data source query.
   */
  final MetaData getMetaData() {
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
   * Get SQL code for query.
   * @return SQL code for the database query.
   */
  public final String getSQLForQuery() {
    return querySQL;
  }

  /**
   * Execute query.
   * @param callInfo Call info.
   * @param takeSnapshot Indicates that a snapshot should be taken.
   * @return Result of query.
   */
  final DataSet executeQuery(CallInfo callInfo, boolean takeSnapshot) {
    DataSet data = new DataSet(this);
    return db.access(callInfo, () -> {
      try (WrappedStatement ws = db.compile(getSQLForQuery())) {
        proceedWithQuery(ws.getStatement(), data);
        if (takeSnapshot) {
          setSnapshot(data);
          db.logSnapshot(callInfo, data);
        } else {
          db.logQuery(callInfo, data);
        }
      }
      return data;
    });
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
    s.setReadOnly();
    snapshot = s;
  }

  /**
   * Execute query.
   * @param queryStmt Query statement.
   * @param ds Target data set.
   * @throws SQLException if a database error occurs.
   */
  private void proceedWithQuery
  (PreparedStatement queryStmt, DataSet ds) throws SQLException {
    if (queryArgs != null && queryArgs.length > 0) {
      for (int i=0; i < queryArgs.length; i++) {
        queryStmt.setObject(i + 1, queryArgs[i]);
      }
    }
    try(ResultSet rs = queryStmt.executeQuery()) {
      int colCount = metaData.getColumnCount();
      while (rs.next()) {
        Object[] data = new Object[colCount];
        for (int i = 0; i < colCount; i++) {  
          data[i] = rs.getObject(i+1);
        }
        ds.addRow(new Row(data));
      }
    }
  }

  /**
   * Set dirty status.
   * 
   * <p>
   * This method is used by {@link JDBDT#populateIfChanged}
   * and {@link JDBDT#assertUnchanged(DataSource)}.
   * </p>
   * 
   * @param dirty Status.
   */
  final void setDirtyStatus(boolean dirty) {
    this.dirty = dirty;
  }

  /**
   * Get dirty status.
   * 
   * <p>
   * This method is used by {@link JDBDT#populateIfChanged}
   * to check if the table has a dirty status.
   * </p>.
   * 
   * @return The dirty status.
   */
  final boolean getDirtyStatus() {
    return dirty;
  }

}

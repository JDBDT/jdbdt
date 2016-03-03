package org.jdbdt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Observer of database changes, from which deltas can be derived.
 *
 * <p>
 * Observers and deltas for observers are created 
 * through the {@link JDBDT} facade (check the <code>observe</code> and
 * <code>verify</code> methods).
 * </p>
 *
 * @since 0.1
 */
public class Observer {

  /**
   * Prepared statement for query.
   */
  private final PreparedStatement queryStmt;
  
  /**
   * Query arguments.
   */
  private final Object[] queryArgs;
  
  /**
   * Meta-data for statement.
   */
  private final MetaData metaData;
  
  /**
   * Last observed state.
   */
  private RowSet lastObserved;

  /**
   * Directory for delta logs, if any is set.
   */
  private Log errorLog = null;

  /**
   * Constructs a observer for a given table.
   * If the given row set is non-null it will be assumed 
   * as the initial observation state. Otherwise,
   * a table query will be executed upon construction.
   * 
   * @param t Table.
   * @param initial Initial row set to assume (ignored if null).
   */
  Observer(Table t, RowSet initial) {
    this(t.getQuery(), t.getMetaData(), null, initial);
  }
  
  /**
   * Constructs a observer for a given query.
   * If the given row set is non-null it will be assumed 
   * as the initial observation state. Otherwise,
   * the query will be executed upon construction.
   * 
   * @param query Database query.
   * @param queryArgs Query arguments.
   * @param initial Initial row set to assume (ignored if null).
   */
  Observer(Query query, Object[] queryArgs, RowSet initial) {
    this(query.getStatement(), new MetaData(query.getStatement()), queryArgs, initial);
  }
  
  /**
   * Constructs an observer for a given query.
   * @param queryStmt Query statement.
   * @param queryMD Query meta-data.
   * @param queryArgs Query arguments.
   * @param initial Initial row set to assume.
   */
  private Observer(PreparedStatement queryStmt, MetaData queryMD, Object[] queryArgs, RowSet initial) {
    this.queryStmt = queryStmt;
    this.queryArgs = queryArgs;
    this.metaData = queryMD;
    this.lastObserved = initial;
    if (initial == null) {
      refresh();
    }
  }

  /**
   * Get meta-data for observer query.
   * @return Meta-data instance.
   */
  MetaData getMetaData() {
    return metaData;
  }
  
  /**
   * Get error log (if set).
   * @return A log instance or null if logging is not enabled.
   */
  Log getErrorLog() {
    return errorLog;
  }
  
  /**
   * Get row set for last observation.
   * @return The set of rows corresponding to the last executed query.
   */
  RowSet getLastObservation() {
    return lastObserved;
  }
  
  /**
   * Enable assertion error logging.
   * 
   * <p>
   * This method specifies that whenever a 
   * assertion error occurs during delta verification,
   * a log should be generated informing the 
   * part of the delta that has not been validated
   * (through {@link Log#write(Delta)}.
   * </p>
   *
   *
   * @param log Log instance.
   * @see Log#write(Delta)
   * @see JDBDT#log(java.io.PrintStream)
   * @see JDBDT#log(java.io.File)
   */
  final void logErrorsTo(Log log) {
    errorLog = log; 
  }

  /**
   * Refresh observer and yield a delta for validation.
   *
   * <p>
   * The database will be read for a fresh observation state,
   * and the returned delta will represent the differences between
   * the current and previous database states.
   * </p>
   * 
   * <p>
   * Unless you wish to use the observer in a specific context,
   * you should use the <code>verify(obsInstance)</code>
   * idiom (see {@link JDBDT#verify(Observer)}) 
   * instead of calling this method directly.
   * </p>
   * 
   * @return The database delta for validation.
   */
  Delta getDelta() {
    RowSet previous = lastObserved;
    refresh();
    Delta delta = new Delta(this, previous, lastObserved);
    return delta;
  }
  
  /**
   * Refresh observation state, by re-executing the query.
   * yielding a new data set.
   */
  final void refresh() {
    try { 
      if (queryArgs != null && queryArgs.length > 0) {
        for (int i=0; i < queryArgs.length; i++) {
          queryStmt.setObject(i + 1, queryArgs[i]);
        }
      }
      ResultSet rs = queryStmt.executeQuery();
      int colCount = metaData.getColumnCount();
      RowSet ds = new RowSet();  
      while (rs.next()) {
        Object[] data = new Object[colCount];
        for (int i = 0; i < colCount; i++) {          
          data[i] = rs.getObject(i+1);
        }
        ds.addRow(new RowImpl(data));
      }
      lastObserved = ds;
    } 
    catch(SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
  }
}
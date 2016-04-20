package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Database instance.
 * 
 * @since 0.1
 *
 */
public final class DB {
  /**
   * Database options.
   *
   * @since 0.1
   */
  public enum Option {
    /**
     * Statement pooling (enabled by default).
     */
    STATEMENT_POOLING,
    /**
     * Log deltas.
     */
    LOG_DELTAS,
    /**
     * Log assertion errors.
     */
    LOG_ASSERTION_ERRORS,
    /**
     * Log data set insertions.
     */
    LOG_INSERTIONS,
    /**
     * Log data set snapshots.
     */
    LOG_QUERIES,
    /**
     * Log SQL statements.
     */
    LOG_SQL;
  }
  /**
   * Connection.
   */
  private final Connection connection;

  /**
   * Trace options.
   */
  private EnumSet<Option> optionSet = EnumSet.noneOf(Option.class);

  /**
   * Log to use. 
   */
  private Log log = null;

  /**
   * Statement pool.
   */
  private Map<String,PreparedStatement> pool;


  /**
   * Constructor.
   * @param connection Database connection.
   */
  DB(Connection connection) {
    this.connection = connection;
    log = new Log(System.err);
    enable(Option.STATEMENT_POOLING);
  }

  /**
   * Enable options.
   * @param options Options to enable.
   */
  public void enable(Option... options) {
    for (Option o : options) {
      optionSet.add(o);
    }
  }
  
  /** 
   * Enable all logging options.
   */
  public void enableFullLogging() {
    enable(DB.Option.LOG_ASSERTION_ERRORS,
           DB.Option.LOG_DELTAS,
           DB.Option.LOG_INSERTIONS,
           DB.Option.LOG_QUERIES,
           DB.Option.LOG_SQL);
  }

  /**
   * Disable options.
   * @param options Options to enable.
   * @see #enable(Option...)
   * @see #isEnabled(Option)
   */
  public void disable(Option... options) {
    for (Option o : options) {
      optionSet.remove(o);
    }
  }
  
  
  /**
   * Check if option is enabled.
   * @param o Option.
   * @return <code>true</code> if <code>o</code> is enabled.
   * @see #enable(Option...)
   * @see #disable(Option...)
   */
  public boolean isEnabled(Option o) {
    return optionSet.contains(o);
  }

  /**
   * Get connection.
   * @return The connection associated to this instance.
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Create a table data source.
   * @param name Table name.
   * @return A new {@link Table} instance.
   */
  public Table table(String name) {
    return new Table(this, name);
  }

  /**
   * Create a query data source.
   * @return A new {@link Query} instance.
   */
  public Query select() {
    return new Query(this);
  }

  /**
   * Create a new data source from arbitrary SQL code.
   * @param sql SQL code for query.
   * @return A new {@link SQLDataSource} instance.
   */
  public SQLDataSource source(String sql) {
    return new SQLDataSource(this, sql);
  }
  
  /**
   * Prepare a SQL statement.
   * @param sql SQL code.
   * @return Prepared statement.
   * @throws SQLException If there is a error preparing the statement.
   */
  PreparedStatement 
  compile(String sql) throws SQLException {    
    if (! isEnabled(Option.STATEMENT_POOLING)) {
      logSQL(sql);
      return connection.prepareStatement(sql);
    }
    if (pool == null) {
      pool = new IdentityHashMap<>();
    } 
    String sqlI = sql.intern();
    PreparedStatement ps = pool.get(sqlI);
    if (ps == null) {
      logSQL(sqlI);
      ps = connection.prepareStatement(sqlI);
      pool.put(sqlI, ps);
    }
    return ps;
  }


  /**
   * Set log to use.
   * The log set at creation time
   * writes to <code>System.err</code>.
   * @param log Logging instance.
   */
  public void setLog(Log log) {
    this.log = log;
  }

  
  /**
   * Log delta.
   * @param callInfo  Call info.
   * @param delta Delta instance.
   */
  void logDelta(CallInfo callInfo, Delta delta) {
    if (isEnabled(Option.LOG_DELTAS)) {
      log.write(callInfo, delta);
    }
  }
  
  /**
   * Log snapshot.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void logQuery(CallInfo callInfo, DataSet data) {
    if (isEnabled(Option.LOG_QUERIES)) {
      log.write(callInfo, data);
    }
  }
  
  /**
   * Log insertion.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void logInsertion(CallInfo callInfo, DataSet data) {
    if (isEnabled(Option.LOG_INSERTIONS)) {
      log.write(callInfo, data);
    }
  }
  
  @SuppressWarnings("javadoc")
  void logSQL(String sql) {
    if (isEnabled(Option.LOG_SQL)) {
      log.writeSQL(sql);
    }
  }


}

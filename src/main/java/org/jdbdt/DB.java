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
    StatementPooling,
    /**
     * Log deltas.
     */
    LogDeltas,
    /**
     * Log assertion errors.
     */
    LogAssertionErrors,
    /**
     * Log data set insertions.
     */
    LogInsertions,
    /**
     * Log data set snapshots.
     */
    LogSnapshots,
    /**
     * Log SQL statements.
     */
    LogSQL;
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
    enable(Option.StatementPooling);
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
    enable(DB.Option.LogAssertionErrors,
           DB.Option.LogDeltas,
           DB.Option.LogSnapshots,
           DB.Option.LogSQL);
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
   * @return A new {@link Table} instance.
   */
  public Query select() {
    return new Query(this);
  }

  /**
   * Prepare a SQL statement.
   * @param sql SQL code.
   * @return Prepared statement.
   * @throws SQLException If there is a error preparing the statement.
   */
  public PreparedStatement 
  compile(String sql) throws SQLException {    
    if (! isEnabled(Option.StatementPooling)) {
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
   * @param delta Delta instance.
   */
  void logDelta(Delta delta) {
    if (isEnabled(Option.LogDeltas)) {
      log.write(delta);
    }
  }
  
  /**
   * Log snapshot.
   * @param data Data set.
   */
  void logSnapshot(DataSet data) {
    if (isEnabled(Option.LogSnapshots)) {
      log.write(data);
    }
  }
  
  /**
   * Log insertion.
   * @param data Data set.
   */
  void logInsertion(DataSet data) {
    if (isEnabled(Option.LogInsertions)) {
      log.write(data);
    }
  }
  
  @SuppressWarnings("javadoc")
  void logSQL(String sql) {
    if (isEnabled(Option.LogSQL)) {
      log.writeSQL(sql);
    }
  }


}

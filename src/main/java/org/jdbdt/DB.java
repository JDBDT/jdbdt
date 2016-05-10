package org.jdbdt;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Database handle.
 * 
 * <p>
 * An object of this kind is used to encapsulate access
 * to a database connection.
 * </p>
 * 
 * @see JDBDT#database(Connection)
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
     * Log assertions (both failed and passed).
     */
    LOG_ASSERTIONS,
    /**
     * Log assertion errors (only for failed assertions).
     */
    LOG_ASSERTION_ERRORS,
    /**
     * Log database queries. 
     */
    LOG_QUERIES,
    /**
     * Log database setup operations.
     */
    LOG_SETUP,
    /**
     * Log database snapshots. 
     */
    LOG_SNAPSHOTS,
    /**
     * Reuse statements (enabled by default).
     */
    REUSE_STATEMENTS;
  }
  
  /**
   * Connection.
   */
  private final Connection connection;

  /**
   * Trace options.
   */
  private final EnumSet<Option> optionSet = EnumSet.noneOf(Option.class);

  /**
   * Log to use. 
   */
  private Log log = null;

  /**
   * Statement pool.
   */
  private Map<String, PreparedStatement> pool;

  /**
   * Savepoint, if set.
   */
  private Savepoint savepoint;

  /**
   * Constructor.
   * @param connection Database connection.
   */
  DB(Connection connection) {
    this.connection = connection;
    log = new Log(System.err);
    enable(Option.REUSE_STATEMENTS);
    enable(Option.LOG_ASSERTION_ERRORS);
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
           DB.Option.LOG_ASSERTIONS,
           DB.Option.LOG_SETUP,
           DB.Option.LOG_QUERIES,
           DB.Option.LOG_SNAPSHOTS);
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
   * Prepare a SQL statement.
   * @param sql SQL code.
   * @return Prepared statement.
   * @throws SQLException If there is a error preparing the statement.
   */
  PreparedStatement 
  compile(String sql) throws SQLException {    
    if (! isEnabled(Option.REUSE_STATEMENTS)) {
      return connection.prepareStatement(sql);
    }
    if (pool == null) {
      pool = new IdentityHashMap<>();
    } 
    String sqlI = sql.intern();
    PreparedStatement ps = pool.get(sqlI);
    if (ps == null) {
      ps = connection.prepareStatement(sqlI);
      pool.put(sqlI, ps);
    }
    return ps;
  }


  /**
   * Redirect log output to a stream.
   * The log set at creation time
   * writes to <code>System.err</code>.
   * @param out Output stream.
   */
  public void setLog(PrintStream out) {
    this.log = new Log(out);
  }

  /**
   * Set output file for log output.
   * The log set at creation time
   * writes to <code>System.err</code>.
   * @param outputFile Logging instance.
   * @throws IOException If the file cannot be opened.
   */
  public void setLog(File outputFile) throws IOException {
    this.log = new Log(outputFile);
  }

  /**
   * Log query result.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void logQuery(CallInfo callInfo, DataSet data) {
    if (isEnabled(Option.LOG_QUERIES)) {
      log.write(callInfo, data);
    }
  }
  
  /**
   * Log query result.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void logSnapshot(CallInfo callInfo, DataSet data) {
    if (isEnabled(Option.LOG_SNAPSHOTS)) {
      log.write(callInfo, data);
    }
  }
  
  /**
   * Log insertion.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void logInsertion(CallInfo callInfo, DataSet data) {
    if (isEnabled(Option.LOG_SETUP)) {
      log.write(callInfo, data);
    }
  }

  /**
   * Log delta assertion.
   * @param callInfo Call info.
   * @param da Delta assertion.
   */
  void log(CallInfo callInfo, DeltaAssertion da) {
    if (isEnabled(Option.LOG_ASSERTIONS) ||
        (    ! da.passed() 
          && isEnabled(Option.LOG_ASSERTION_ERRORS) )) {
      log.write(callInfo, da);
    }
  }
  
  /**
   * Log state assertion.
   * @param callInfo Call info.
   * @param sa state assertion.
   */
  void log(CallInfo callInfo, StateAssertion sa) {
    if (isEnabled(Option.LOG_ASSERTIONS) ||
        (    ! sa.passed() 
          && isEnabled(Option.LOG_ASSERTION_ERRORS) )) {
      log.write(callInfo, sa);
    }
  }

  /**
   * Log database setup command.
   * @param callInfo Call info.
   * @param sql SQL code.
   */
  void logSetup(CallInfo callInfo, String sql) {
    if (isEnabled(Option.LOG_SETUP)) {
      log.writeSQL(callInfo, sql);
    }
  }
  /**
   * Set JDBDT save-point.
   * @param callInfo Call info.
   */
  void save(CallInfo callInfo) {
    try {
      if (connection.getAutoCommit()) {
        throw new InvalidOperationException("Auto-commit is set for database connection.");
      }
      if (savepoint != null) {
        connection.releaseSavepoint(savepoint);
      }
      savepoint = connection.setSavepoint(SAVEPOINT_ID);
    } catch (SQLException e) {
      throw new DBExecutionException(e);
    }
    logSetup(callInfo, "savepoint");
  }
  
  /**
   * Commit changes in the current transaction.
   * @param callInfo Call info.
   */
  void commit(CallInfo callInfo) {
    try {
      if (savepoint != null) {
        connection.releaseSavepoint(savepoint);
        savepoint = null;
      }
      connection.commit();
    } 
    catch(SQLException e) {
      throw new DBExecutionException(e); 
    }
  }
  
  /**
   * Roll back changes to JDBDT save-point.
   * @param callInfo Call info.
   */
  void restore(CallInfo callInfo) {
    try {
      if (savepoint == null) {
        throw new InvalidOperationException("Save point is not set.");
      }
      connection.rollback(savepoint);
    } 
    catch(SQLException e) {
      throw new DBExecutionException(e); 
    }
  }
  /**
   * Save-point id constant.
   */
  private static final String SAVEPOINT_ID = "_jdbdtSavepoint_";

}

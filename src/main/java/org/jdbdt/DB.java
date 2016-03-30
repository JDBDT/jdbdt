package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Database instance.
 * 
 * @since 0.1
 *
 */
public final class DB {
   /**
    * Connection.
    */
  private final Connection connection;
 
  /**
   * Trace options.
   */
  private EnumSet<Trace> traceOptions = EnumSet.noneOf(Trace.class);
  
  /**
   * Trace log.
   */
  private Log traceLog = null;
  
  /**
   * Statement pool.
   */
  private Map<String,PreparedStatement> pool;
  
  /**
   * Statement pooling flag.
   */
  private boolean statementPooling = true;
  
  /**
   * Constructor.
   * @param connection Database connection.
   */
  DB(Connection connection) {
    this.connection = connection;
  }
  
  /**
   * Set trace options for database.
   * @param log Log instance to use.
   * @param options Trace options.
   * @return <code>this</code> (for chained calls).
   */
  public DB trace(Log log, Trace... options) {
    traceLog = log;
    traceOptions.clear();
    for (Trace o : options) {
      traceOptions.add(o);
    }
    return this;
  }
  
  
  @SuppressWarnings("javadoc")
  void trace(Trace option, Consumer<Log> action) {
    if (traceOptions.contains(option))
      action.accept(traceLog);
  }
  
  @SuppressWarnings("javadoc")
  void trace(Trace option, DataSet data) {
    trace(option, l -> l.write(data));
  }
  
  /**
   * Disable statement pooling.
   * @return <code>this</code> (for chained calls).
   */
  public DB disableStatementPooling() {
    if (statementPooling) {
      if (pool != null) {
        pool.clear();
        pool = null;
      }
      statementPooling = false;
    }
    return this;
  }
  
  /**
   * Enable statement pooling.
   * @return <code>this</code> (for chained calls).
   */
  public DB enableStatementPooling() {
    if (!statementPooling) {
      statementPooling = true;
    }
    return this; 
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
    if (! statementPooling) {
      Runtime.getRuntime().logSQL(sql);
      return connection.prepareStatement(sql);
    }
    if (pool == null) {
      pool = new IdentityHashMap<>();
    } 
    sql = sql.intern();
    PreparedStatement ps = pool.get(sql);
    if (ps == null) {
      Runtime.getRuntime().logSQL(sql);
      ps = connection.prepareStatement(sql);
      pool.put(sql, ps);
    }
    return ps;
  }


}

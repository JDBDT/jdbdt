package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;

/**
 * Statement pool.
 * 
 * @since 0.1
 */
final class StatementPool {

  /**
   * Enabled flag.
   */
  private boolean poolingEnabled;

  /**
   * The pool.
   */
  private 
  Map<Connection, Map<String,PreparedStatement> > 
  pools;


  /**
   * Private constructor, to prevent outside instantiation.
   */
  private StatementPool() {
    poolingEnabled = true;
  }

  /**
   * Enable/disable pooling.
   * @param enable Enabling flag.
   */
  private synchronized void pooling(boolean enable) {
    if (enable != poolingEnabled) {
      poolingEnabled = enable;
      if (!enable && pools != null) {
        pools.clear();
        pools = null;
      }
    }
  }

  /**
   * Compile statement.
   * @param c connection
   * @param sql SQL code.
   * @return Compiled statement.
   * @throws SQLException If there is an error compiling the statement.
   */
  private synchronized PreparedStatement 
  compileStatement(Connection c, String sql) throws SQLException {    
    if (! poolingEnabled) {
      return c.prepareStatement(sql);
    }
    Map<String, PreparedStatement> pool;
    if (pools == null) {
      pools = new WeakHashMap<>();
      pool = null;
    } else {
      pool = pools.get(c);
    }
    if (pool == null) {
      pool = new IdentityHashMap<>();
      pools.put(c, pool);
    } 
    sql = sql.intern();
    PreparedStatement ps = pool.get(sql);
    if (ps == null) {
      ps = c.prepareStatement(sql);
      pool.put(sql, ps);
    }
    return ps;
  }

  /** 
   * Singleton instance.
   */
  private static final StatementPool 
  INSTANCE = new StatementPool();

  /**
   * Activate statement pooling.
   */
  static void enable() {
    INSTANCE.pooling(true);
  }
  /**
   * Deactivate statement pooling.
   */
  static void disable() {
    INSTANCE.pooling(false);
  }
  /**
   * Compile an SQL statement.
   * 
   * <p>
   * The method yields a previously compiled statement (re-using it)
   * if the SQL code was already compiled for the given connection.
   * The method is thread-safe.
   * </p>
   * 
   * @param c Database connection.
   * @param sql SQL code.
   * @return A compiled statement.
   * @throws SQLException if an error occurs compiling the statement.
   */
  static PreparedStatement compile(Connection c, String sql) throws SQLException {
    return INSTANCE.compileStatement(c,  sql);
  }
}

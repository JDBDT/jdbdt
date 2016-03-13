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
   * Disable pooling.
   */
  private synchronized void noPooling() {
    poolingEnabled = false;
    if (pools != null) {
      pools.clear();
      pools = null;
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
   * Disable statement pooling.
   */
  static void disablePooling() {
    INSTANCE.noPooling();
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

  /**
   * Get INSERT statement for a table.
   * @param t Table.
   * @return Compiled statement for insertion.
   * @throws SQLException If a database error occurs.
   */
  static PreparedStatement insert(Table t) throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    String[] columnNames = t.getColumnNames();
    sql.append(t.getName())
    .append('(')
    .append(columnNames[0]);
    for (int i=1; i < columnNames.length; i++) {
      sql.append(',').append(columnNames[i]);
    }
    sql.append(") VALUES (?");
    for (int i=1; i < columnNames.length; i++) {
      sql.append(",?");
    }
    sql.append(')');
    return compile(t.getConnection(), sql.toString());
  }


  /**
   * Get DELETE statement for a table.
   * @param t Table.
   * @return Compiled statement for deletion.
   * @throws SQLException If a database error occurs.
   */
  static PreparedStatement delete(Table t) throws SQLException {
    return compile(t.getConnection(),
        "DELETE FROM " + t.getName());
  }

  /**
   * Get TRUNCATE statement for a table.
   * @param t Table.
   * @return Compiled statement for truncation.
   * @throws SQLException If a database error occurs.
   */
  static PreparedStatement truncate(Table t) throws SQLException {
    return compile(t.getConnection(),
        "TRUNCATE TABLE " + t.getName());
  }
}

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
   * Private constructor, to prevent instantiation.
   */
  private StatementPool() { }
  
  /**
   * The pool.
   */
  private static final
  Map<Connection, Map<String,PreparedStatement> > 
     POOLS = new WeakHashMap<>();
  
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
    Map<String, PreparedStatement> pool;
    synchronized (POOLS) {
      pool = POOLS.get(c);
      if (pool == null) {
        pool = new IdentityHashMap<>();
        POOLS.put(c, pool);
      }
    }
    synchronized(pool) {
      sql = sql.intern();
      PreparedStatement ps = pool.get(sql);
      if (ps == null) {
        ps = c.prepareStatement(sql);
        pool.put(sql, ps);
      }
      return ps;
    }
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

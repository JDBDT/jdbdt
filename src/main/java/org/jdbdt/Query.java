package org.jdbdt;

/**
 * Data source created from arbitrary SQL query statement.
 *
 * @since 0.1 
 *
 */
final class Query extends DataSource {

  /**
   * SQL code for query.
   */
  private final String sqlForQuery; 
  
  /**
   * Constructor.
   * @param db Database handle.
   * @param sql SQL for query
   * @param args Optional arguments for query.
   */
  Query(DB db, String sql, Object... args) {
    super(db, args);
    this.sqlForQuery = sql;
  }
  
  @Override
  String getSQLForQuery() {
    return sqlForQuery;
  } 
}

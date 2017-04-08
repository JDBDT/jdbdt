package org.jdbdt;

/**
 * Query data source.
 *
 * @see JDBDT#query(DB, String, Object...)
 * @see JDBDT#select(String...)
 * @see QueryBuilder
 * 
 * @since 0.1 
 *
 */
public final class Query extends DataSource {

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
  public String getSQLForQuery() {
    return sqlForQuery;
  } 
  
}

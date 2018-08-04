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
   * Constructor.
   * @param db Database handle.
   * @param sql SQL for query
   * @param args Optional arguments for query.
   */
  public Query(DB db, String sql, Object... args) {
    super(db, sql, args);
  }
}

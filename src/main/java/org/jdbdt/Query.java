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
    this(CallInfo.create(), db, sql, args);
  }
  
  /**
   * Constructor with supplied call information.
   * @param callInfo Call info.
   * @param db Database handle.
   * @param sql SQL for query
   * @param args Optional arguments for query.
   */
  Query(CallInfo callInfo, DB db, String sql, Object... args) {
    super(callInfo, db, sql, args);
  }
}

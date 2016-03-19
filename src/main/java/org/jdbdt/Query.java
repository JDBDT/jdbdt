package org.jdbdt;

import java.sql.Connection;

/**
 * Representation of a table query.
 *
 * <p>
 * A table query is created using {@link JDBDT#selectFrom(Table)}
 * or implicitly through {@link JDBDT#snapshot(DataSource)}.
 * It has an associate {@link Table} instance, set at creation time, plus
 * optional WHERE, GROUP BY and HAVING clauses. The optional
 * statements may be set using {@link #where(String)}, {@link #groupBy(String...)}),
 * and {@link #having(String)}, usually in a chained sequence of calls.
 * </p>
 * 
 * <p><b>Illustration 1 - simple query to select all columns</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Table;
 * import org.jdbdt.TableQuery;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Table t = table("MyTable")
 *          .columns("A", "B", "C")
 *          .boundTo(conn);
 * TableQuery q = selectFrom(t);         
 * </pre></blockquote>
 * 
 * <p><b>Illustration 2 - query with a WHERE clause</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Table;
 * import org.jdbdt.TableQuery;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Table t = table("MyTable")
 *          .columns("A", "B", "C")
 *          .boundTo(conn);
 * int lo = 0, hi = 100;     
 * TableQuery q = selectFrom(t)
 *               .where("A &gt; ? AND A &lt; ?").
 *               .withArguments(lo, hi);         
 * </pre></blockquote>
 * 
 * <p><b>Illustration 3 - query with WHERE, GROUP BY, and HAVING clauses</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Table;
 * import org.jdbdt.TableQuery;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Table t = table("MyTable")
 *          .columns("A", "B")
 *          .boundTo(conn);
 * TableQuery q = selectFrom(t)
 *               .where("A &gt; 10")
 *               .groupBy("A") 
 *               .having("SUM(C) &gt; 100");  
 * </pre></blockquote>
 * 
 * 
 * @see Table
 * @see JDBDT#selectFrom(Table)
 * 
 * @since 0.1
 */
public final class Query extends DataSource {

  /**
   * Table.
   */
  private final Table table;

  /**
   * Query-specific columns (equal to table columns if null).
   */
  private String[] columns;

  /**
   * WHERE statement for query (undefined if null).
   */
  private String whereClause = null;

  /**
   * GROUP BY clause for query (undefined if null).
   */
  private String groupByClause = null;

  /**
   * HAVING clause for query (undefined if null).
   */
  private String havingClause = null;

  /**
   * ORDER BY clause for query (undefined if null).
   */
  private String orderByClause = null;

  /**
   * DISTINCT clause (undefined if false).
   */
  private boolean distinctClause;
  
  
  /**
   * Query arguments if any.
   */
  private Object[] queryArgs = null;

  /**
   * Constructs a new query.
   * @param table Database table.
   */
  Query(Table table) {
    table.checkIfBound();
    this.table = table;
  }

  /**
   * Get table associated to query.
   * @return Table set for the query.
   */
  Table getTable() {
    return table;
  }

  /**
   * Set columns for query.
   * 
   * <p>
   * If the query columns are not set, the source table
   * columns (see {@link Table#columns(String...)})
   * will be used by the query.
   * </p>
   * 
   * @param columnNames Column names.
   * @return The query instance for chained calls.
   */
  public Query columns(String... columnNames) {
    checkNotCompiled();
    if (columns != null) {
      throw new InvalidUsageException("Columns already set.");
    }
    columns = columnNames.clone();
    return this;
  }

  /**
   * Set WHERE clause for query.
   * @param clause String for WHERE clause.
   * @return The query instance for chained calls.
   */
  public Query where(String clause) {
    checkNotCompiled();
    if (whereClause != null) {
      throw new InvalidUsageException("WHERE clause already set.");
    }
    whereClause = clause;
    return this;
  }
  
  /**
   * Set DISTINCT clause for query.
   * @return The query instance for chained calls.
   */
  public Query distinct() {
    checkNotCompiled();
    if (distinctClause) {
      throw new InvalidUsageException("DISTINCT clause already set.");
    }
    distinctClause = true;
    return this;
  }


  /**
   * Set GROUP BY clause for query.
   * @param fields GROUP BY fields.
   * @return The query instance for chained calls.
   */
  public Query groupBy(String... fields) {
    checkNotCompiled();
    if (groupByClause != null) {
      throw new InvalidUsageException("GROUP BY clause already set.");
    }
    if (fields.length == 0) {
      groupByClause = fields[0];
    } else {
      StringBuilder sb = new StringBuilder(fields[0]);
      for (int i=1; i < fields.length; i++) {
        sb.append(',').append(fields[i]);
      }
      groupByClause = sb.toString();
    }
    return this;
  }

  /**
   * Set ORDER BY clause for query.
   * 
   * <p>
   * Note that setting the ORDER BY clause will not affect 
   * the result of database assertions by JDBDT, since these
   * do not attend to the order of query results.
   * Setting the ORDER BY clause will also likely impact 
   * on the performance of query execution, but may be useful 
   * for debugging purposes.
   * </p>
   * 
   * @param fields ORDER BY fields.
   * @return The query instance for chained calls.
   */
  public Query orderBy(String... fields) {
    checkNotCompiled();
    if (orderByClause != null) {
      throw new InvalidUsageException("GROUP BY clause already set.");
    }
    if (fields.length == 0) {
      orderByClause = fields[0];
    } else {
      StringBuilder sb = new StringBuilder(fields[0]);
      for (int i=1; i < fields.length; i++) {
        sb.append(',').append(fields[i]);
      }
      orderByClause = sb.toString();
    }
    return this;
  }

  /**
   * Set HAVING clause for query.
   * @param clause String for HAVING clause.
   * @return The query instance for chained calls.
   */
  public Query having(String clause) {
    checkNotCompiled();
    if (havingClause != null) {
      throw new InvalidUsageException("HAVING clause already set.");
    }
    havingClause = clause;
    return this;
  }

  /**
   * Set query arguments.
   * @param args Query arguments.
   * @return The query instance for chained calls.
   */
  public Query withArguments(Object... args) {
    checkNotCompiled();
    if (queryArgs != null) {
      throw new InvalidUsageException("Query arguments are already set.");
    }
    if (args == null || args.length == 0) {
      throw new InvalidUsageException("Invalid query arguments.");
    }
    queryArgs = args;
    return this;
  }

  @Override
  final Connection getConnection() {
    return table.getConnection();
  }


  @Override
  final String[] getColumnNames() {
    return columns != null ? columns : table.getColumnNames();
  }

  @Override
  final Object[] getQueryArguments() {
    return queryArgs;
  }

  @Override
  final String getSQLForQuery() {
    StringBuilder sql = new StringBuilder("SELECT");
    if (distinctClause) {
      sql.append(" DISTINCT");
    }
    sql.append("\n ");
    
    // Deal with columns
    String[] colNames = getColumnNames();
    sql.append(colNames[0]);
    for (int i = 1; i < colNames.length;i++) {
      sql.append(',').append('\n').append(' ').append(colNames[i]);
    }
    // Deal with table
    sql.append("\nFROM ").append(table.getName());
    
    // Deal with WHERE, GROUP BY, HAVING, ORDER BY
    if (whereClause != null) {
      sql.append("\nWHERE\n").append(' ').append(whereClause);
    }
    if (groupByClause != null) {
      sql.append("\nGROUP BY\n").append(' ').append(groupByClause);
    }
    if (havingClause != null) {
      sql.append("\nHAVING\n").append(' ').append(havingClause);
    }
    if (orderByClause != null) {
      sql.append("\nORDER BY\n").append(' ').append(orderByClause);
    }
    return sql.toString();
  }

  /**
   * Get WHERE clause.
   * @return The WHERE clause for the query (null if undefined).
   */
  String whereClause() {
    return whereClause;
  }

  /**
   * Get DISTINCT clause.
   * @return <code>true</code> if DISTINCT clause is set for the query.
   */
  boolean distinctClause() {
    return distinctClause;
  }
  
  /**
   * Get GROUP BY clause.
   * @return The WHERE clause for the query (null if undefined).
   */
  String groupByClause() {
    return groupByClause;
  }

  /**
   * Get HAVING clause.
   * @return The HAVING clause for the query (null if undefined).
   */
  String havingClause() {
    return havingClause;
  }

  /**
   * Get ORDER BY clause.
   * @return The ORDER BY clause for the query (null if undefined).
   */
  String orderByClause() {
    return orderByClause;
  }

  /**
   * Get string representation.
   * @return The SQL code for the query.
   */
  @Override
  public String toString() {
    return getSQLForQuery();
  }
}

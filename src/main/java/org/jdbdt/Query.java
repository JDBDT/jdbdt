package org.jdbdt;


/**
 * Representation of a table query.
 *
 * <p>
 * A table query is created using {@link DB#select()}
 * or implicitly through {@link JDBDT#takeSnapshot(DataSource)}.
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
 * 
 * @since 0.1
 */
public final class Query extends DataSource {

  /**
   * FROM clause.
   */
  private String[] fromClause = null;

  /**
   * WHERE clause (undefined if null).
   */
  private String whereClause = null;

  /**
   * GROUP BY clause for query (undefined if null).
   */
  private String[] groupByClause = null;

  /**
   * HAVING clause for query (undefined if null).
   */
  private String havingClause = null;

  /**
   * ORDER BY clause for query (undefined if null).
   */
  private String[] orderByClause = null;

  /**
   * DISTINCT clause (undefined if false).
   */
  private boolean distinctClause;

  /**
   * Constructs a new query.
   * @param db Database instance.
   */
  Query(DB db) {
    super(db);
  }


  /**
   * Set FROM clause.
   * @param columns Query Columns.
   * @return The The query instance for chained calls.
   */
  @SafeVarargs
  public final Query columns(String... columns) {
    super.setColumns(columns);
    return this;
  }

  /**
   * Set table as the data source to this query.
   * <p>
   * A call to this method is shorthand for <code>from(t.getName())</code>.
   * @param t Table source.
   * @return The query instance for chained calls.
   */
  public Query from(Table t) {
    return from(t.getName());
  }
  
  /**
   * Set FROM clause.
   * @param sources Data sources.
   * @return The query instance for chained calls.
   */
  @SafeVarargs
  public final Query from(String...sources) {
    checkNotCompiled();
    if (fromClause != null) {
      throw new InvalidOperationException("FROM clause already set.");
    }
    this.fromClause = sources.clone();
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
      throw new InvalidOperationException("WHERE clause already set.");
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
      throw new InvalidOperationException("DISTINCT clause already set.");
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
      throw new InvalidOperationException("GROUP BY clause already set.");
    }
    groupByClause = fields.clone();
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
      throw new InvalidOperationException("GROUP BY clause already set.");
    }
    orderByClause = fields.clone();    
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
      throw new InvalidOperationException("HAVING clause already set.");
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
    super.setQueryArguments(args);
    return this;
  }


  @Override
  final String getSQLForQuery() {
    StringBuilder sql = new StringBuilder("SELECT");
    if (distinctClause) {
      sql.append(" DISTINCT");
    }
    sql.append("\n ");
    
    // Deal with columns
    String[] colNames = getColumns();
    if (colNames == null) { 
      sql.append('*');
    } else {
      sql.append(colNames[0]);
      for (int i = 1; i < colNames.length;i++) {
        sql.append(',').append('\n').append(' ').append(colNames[i]);
      }
    }
    // Deal with FROM, WHERE, GROUP BY, HAVING, ORDER BY
    format(sql, "FROM",     fromClause);
    format(sql, "WHERE",    whereClause);
    format(sql, "GROUP BY", groupByClause);
    format(sql, "HAVING",   havingClause);
    format(sql, "ORDER BY", orderByClause);
    return sql.toString();
  }

  @SuppressWarnings("javadoc")
  private void format(StringBuilder sb, String clauseName, String value) {
    if (value != null) {
      sb.append('\n')
      .append(clauseName)
      .append('\n')
      .append(' ')
      .append(value);
    }
  }

  @SuppressWarnings("javadoc")
  private void format(StringBuilder sb, String clauseName, String[] values) {
    if (values != null) {
      format(sb, clauseName, values[0]);
      for (int i=1; i < values.length; i++) {
        sb.append(',').append('\n').append(' ').append(values[i]);
      }
    }
  }
  /**
   * Get FROM clause.
   * @return The WHERE clause for the query (null if undefined).
   */
  String[] fromClause() {
    return fromClause;
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
  String[] groupByClause() {
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
  String[] orderByClause() {
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

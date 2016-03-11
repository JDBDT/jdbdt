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
 * <p>
 * Typed table queries (instances of subclass {@link TypedTable}) can be
 * defined similarly but are defined in association to {@link TypedTable} objects.
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
 * @see TypedTableQuery
 * 
 * @since 0.1
 */
public class TableQuery extends DataSource {

  /**
   * Table.
   */
  private final Table table;

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
   * Query arguments if any.
   */
  private Object[] queryArgs = null;
  
  /**
   * Constructs a new query.
   * @param table Database table.
   */
  TableQuery(Table table) {
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
   * Set WHERE clause for query.
   * @param clause String for WHERE clause.
   * @return The query instance for chained calls.
   */
  public TableQuery where(String clause) {
    checkNotCompiled();
    if (whereClause != null) {
      throw new InvalidUsageException("WHERE clause already set.");
    }
    whereClause = clause;
    return this;
  }


  /**
   * Set GROUP BY clause for query.
   * @param fields GROUP BY fields.
   * @return The query instance for chained calls.
   */
  public TableQuery groupBy(String... fields) {
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
   * Set HAVING clause for query.
   * @param clause String for HAVING clause.
   * @return The query instance for chained calls.
   */
  public TableQuery having(String clause) {
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
  public TableQuery withArguments(Object... args) {
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
  final Object[] getQueryArguments() {
    return queryArgs;
  }
  
  @Override
  final String getSQLForQuery() {
    StringBuilder sql = new StringBuilder("SELECT\n ");
    String[] columnNames = table.getColumnNames();
    if (columnNames ==  null) {
      sql.append('*');
    } else {
      sql.append(columnNames[0]);
      for (int i = 1; i < columnNames.length;i++) {
        sql.append(',').append('\n').append(' ').append(columnNames[i]);
      }
    }
    sql.append("\nFROM ").append(table.getName());
    if (whereClause != null) {
      sql.append("\nWHERE\n").append(' ').append(whereClause);
    }
    if (groupByClause != null) {
      sql.append("\nGROUP BY\n").append(' ').append(groupByClause);
    }
    if (havingClause != null) {
      sql.append("\nHAVING\n").append(' ').append(havingClause);
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
  
  


 
}

/*
 * The MIT License
 *
 * Copyright (c) 2016-2019 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;


/**
 * Query builder.
 * 
 * @see JDBDT#select(String...)
 * @see Query
 * 
 * @since 1.0
 */
public final class QueryBuilder  {
  /**
   * Clauses/parameters that may be set for a builder.
   */
  private enum Param {
    /** Columns (mandatory). */
    COLUMNS {
      @Override
      String sqlKeywords() {
        return "";
      }
      @Override
      boolean mandatory() {
        return true;
      }
    }, 
    /** FROM clause (mandatory) */
    FROM {
      @Override
      boolean mandatory() {
        return true;
      }
    },
    /** WHERE clause */
    WHERE,
    /** DISTINT clause */
    DISTINCT,
    /** GROUP BY clause */
    GROUP_BY {
      @Override
      String sqlKeywords() {
        return "GROUP BY";
      }
    },
    /** HAVING clause */
    HAVING,
    /** 
     * LIMIT clause 
     * @since 1.3 
     **/
    LIMIT,
    /** ORDER BY clause */
    ORDER_BY {
      @Override
      String sqlKeywords() {
        return "ORDER BY";
      }
    },
    /** Query arguments. */
    QUERY_ARGS {
      @Override
      String sqlKeywords() {
        return "";
      }
    };
     
    
    /**
     * Get SQL keyword sequence for parameter.
     * The method is overridden for specific parameters.
     * @return A string value.
     */
    String sqlKeywords() {
      return toString();
    }
    /**
     * Check if the parameter is mandatory.
     * The method is overridden for specific parameters.
     * @return <code>true</code> if the parameter is mandatory.
     */
    boolean mandatory() {
      return false;
    }
    
  }
  
  /**
   * Parameter values.
   */
  private final String[] paramValues = new String[Param.values().length];

  /**
   * Query arguments.
   */
  private Object[] queryArgs = { };
  
  /**
   * Constructs a new query builder.
   */
  public QueryBuilder() {
   
  }
  
  /**
   * Set query columns.
   * @param columns Columns to set.
   * @return The query builder instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder columns(String... columns) {
    set(Param.COLUMNS, columns);
    return this;
  }
  
  /**
   * Set table as the data source to this query.
   * <p>
   * A call to this method is shorthand for <code>from(t.getName())</code>.
   * @param t Table source.
   * @return The query builder instance for chained calls.
   */
  public final QueryBuilder from(Table t) {
    set(Param.FROM, t.getName());
    return this;
  }

  /**
   * Set FROM clause.
   * @param sources Data sources.
   * @return The query builder instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder from(String...sources) {
    set(Param.FROM, sources);
    return this;
  }

  /**
   * Set WHERE clause for query.
   * @param clause String for WHERE clause.
   * @return The query builder instance for chained calls.
   */
  public final QueryBuilder where(String clause) {
    set(Param.WHERE, clause);
    return this;
  }

  /**
   * Set DISTINCT clause for query.
   * @return The query builder instance for chained calls.
   */
  public final QueryBuilder distinct() {
    set(Param.DISTINCT,"");
    return this;
  }

  /**
   * Set GROUP BY clause for query.
   * @param fields GROUP BY fields.
   * @return The query builder instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder groupBy(String... fields) {
    set(Param.GROUP_BY, fields);
    return this;
  }

  /**
   * Set ORDER BY clause for query.
   * 
   * <p>
   * Note that setting the ORDER BY clause will not generally affect 
   * the result of database assertions by JDBDT, since these
   * do not attend to the order of query results. Setting
   * LIMIT in combination with ORDER BY will affect the
   * query results but the order of returned rows (even
   * if limited in number) will not be verified.
   * </p>
   * <p>
   * Setting the ORDER BY clause will also likely impact 
   * on the performance of query execution, but may be useful 
   * for debugging purposes.
   * </p>
   * 
   * @param fields ORDER BY fields.
   * @return The query builder instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder orderBy(String... fields) {
    set(Param.ORDER_BY, fields);
    return this;
  }

  /**
   * Set HAVING clause for query.
   * @param clause String for HAVING clause.
   * @return The query builder instance for chained calls.
   */
  public final QueryBuilder having(String clause) {
    set(Param.HAVING, clause);
    return this;
  }
  
  /**
   * Set LIMIT clause for query.
   * @param n Query limit.
   * @return The query builder instance for chained calls.
   * @throws InvalidOperationException for an invalid LIMIT value (<= 0).
   */
  public final QueryBuilder limit(int n) {
    if ( n <= 0) {
       throw new InvalidOperationException("Invalid LIMIT value: " + n);
    }
    set(Param.LIMIT, Integer.toString(n));
    return this;
  }
  
  /**
   * Set arguments for query.
   * @param args Arguments.
   * @return The query builder instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder arguments(Object...args) {
    set(Param.QUERY_ARGS, "");
    queryArgs = args.clone();
    return this;
  }
  

  /**
   * Build the query.
   * @param db Database handle.
   * @return A new query instance.
   */
  public final Query build(DB db) {
    return new Query(CallInfo.create(), db, toSQL(), queryArgs); 
  }
  
  @SuppressWarnings("javadoc")
  private String toSQL() {
    StringBuilder sql = new StringBuilder("SELECT ");
    build(sql, Param.DISTINCT);
    build(sql, Param.COLUMNS);
    build(sql, Param.FROM);
    build(sql, Param.WHERE);
    build(sql, Param.GROUP_BY);
    build(sql, Param.HAVING);
    build(sql, Param.ORDER_BY);
    build(sql, Param.LIMIT);
    return sql.toString();
  }
  
  @SuppressWarnings("javadoc")
  private void build(StringBuilder sb, Param p) {
    String value = get(p);
    if (value != null) {
      sb.append(p.sqlKeywords()).append(' ').append(value).append(' ');
    }
    else if (p.mandatory()) {
      throw new InvalidOperationException(p + " needs to be set for query building.");
    }
  }

  /**
   * Get textual representation.
   * @return The SQL code for the query being built.
   */
  @Override
  public final String toString() {
    return toSQL();
  }
  
  @SuppressWarnings("javadoc")
  private String get(Param p) {
    return paramValues[p.ordinal()];
  }
  
  @SuppressWarnings("javadoc")
  private void set(Param p, String value) {
    final int idx = p.ordinal();
    if (paramValues[idx] != null) {
      throw new InvalidOperationException(p + " already defined.");
    }
    if (value == null) {
      throw new InvalidOperationException("Null value for parameter");
    }
    paramValues[idx] = value;
  }
  
  @SuppressWarnings("javadoc")
  private void set(Param p, String... values) {
    if (values == null || values.length == 0) {
      throw new InvalidOperationException("Null or empty array argument.");
    }
    set(p, Misc.sqlArgumentList(values));
  }

}

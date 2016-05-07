package org.jdbdt;


/**
 * Query builder.
 * 
 * @see JDBDT#select(DB, String...)
 * @see Query
 * 
 * @since 0.1
 */
public final class QueryBuilder  {

  /**
   * Clauses/parameters that may be set for a builder.
   */
  enum Param {
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
    /** ORDER BY clause */
    ORDER_BY {
      @Override
      String sqlKeywords() {
        return "ORDER BY";
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
   * Database handle.
   */
  private final DB db;
  
  /**
   * Parameter values.
   */
  private final String[] paramValues = new String[Param.values().length];

  /**
   * Constructs a new query.
   * @param db Database handle.
   * @param columns Columns for query.
   */
  QueryBuilder(DB db, String... columns) {
    this.db = db;
    set(Param.COLUMNS, columns);
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
    StringBuilder sb = new StringBuilder(values[0]);
    for (int i=1; i < values.length; i++) {
      sb.append(',').append(' ').append(values[i]);
    }
    set(p, sb.toString());
  }


  /**
   * Set table as the data source to this query.
   * <p>
   * A call to this method is shorthand for <code>from(t.getName())</code>.
   * @param t Table source.
   * @return The query instance for chained calls.
   */
  public QueryBuilder from(Table t) {
    set(Param.FROM, t.getName());
    return this;
  }

  /**
   * Set FROM clause.
   * @param sources Data sources.
   * @return The query instance for chained calls.
   */
  @SafeVarargs
  public final QueryBuilder from(String...sources) {
    set(Param.FROM, sources);
    return this;
  }

  /**
   * Set WHERE clause for query.
   * @param clause String for WHERE clause.
   * @return The query instance for chained calls.
   */
  public QueryBuilder where(String clause) {
    set(Param.WHERE, clause);
    return this;
  }

  /**
   * Set DISTINCT clause for query.
   * @return The query instance for chained calls.
   */
  public QueryBuilder distinct() {
    set(Param.DISTINCT,"");
    return this;
  }


  /**
   * Set GROUP BY clause for query.
   * @param fields GROUP BY fields.
   * @return The query instance for chained calls.
   */
  public QueryBuilder groupBy(String... fields) {
    set(Param.GROUP_BY, fields);
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
  public QueryBuilder orderBy(String... fields) {
    set(Param.ORDER_BY, fields);
    return this;
  }

  /**
   * Set HAVING clause for query.
   * @param clause String for HAVING clause.
   * @return The query instance for chained calls.
   */
  public QueryBuilder having(String clause) {
    set(Param.HAVING, clause);
    return this;
  }

  /**
   * Build the query.
   * @param args Optional query arguments.
   * @return A new query instance.
   */
  public Query build(Object... args) {
    return new Query(db, toSQL(), args); 
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
  public String toString() {
    return toSQL();
  }
}

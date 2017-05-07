package org.jdbdt;


/**
 * Table builder.
 * 
 * 
 * @see Table
 * 
 * @since 0.8
 */
public final class TableBuilder  {

  /**
   * Parameters that may be set for a builder.
   */
  private enum Param {
    /** Name. */
    NAME,
    /** Columns. */
    COLUMNS;
  }
  
  /**
   * Parameter values.
   */
  private final String[] paramValues = new String[Param.values().length];

  /**
   * Constructs a new table builder.
   */
  public TableBuilder() {
    
  }
  
  /**
   * Set table name. 
   * @param name Name for the table.
   * @return The builder instance for chained calls.
   */
  public final TableBuilder name(String name) {
    set(Param.NAME, name);
    return this;
  }

  /**
   * Set columns. 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  @SafeVarargs
  public final TableBuilder columns(String... columns) {
    set(Param.COLUMNS, columns);
    return this;
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
      throw new InvalidOperationException("Null value for parameter " + p);
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
   * Build the table object.
   * @param db Database.
   * @return A new {@link Table} instance.
   */
  public Table build(DB db) {
    String name = get(Param.NAME);
    if (name == null) {
      throw new InvalidOperationException("Table name has not been set!");
    }
    
    String cols = get(Param.COLUMNS);
    if (cols == null) {
      throw new InvalidOperationException("Table columns have not been set!");
    }
    
    return new Table
           (db,
            name, 
            String.format("SELECT %s FROM %s", cols, name));
  }

}

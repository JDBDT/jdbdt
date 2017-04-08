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
    NAME (true),
    
    /** Columns. */
    COLUMNS (true),
    
    /** Primary key */
    PRIMARY_KEY (false);
    
    /** Mandatory flag. */
    private boolean mandatory;
    /** Constructor. 
     * @param mandatory Indicates if parameter is mandatory.
     */
    Param(boolean mandatory) {
      this.mandatory = mandatory;
    }
    
    /**
     * Checks if parameter is mandatory.
     * @return <code>true</code> if parameter is mandatory.
     */
    @SuppressWarnings("unused")
    boolean isMandatory() {
      return mandatory;
    }
    
    
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
  public TableBuilder name(String name) {
    set(Param.NAME, name);
    return this;
  }

  /**
   * Set columns. 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  public TableBuilder columns(String... columns) {
    set(Param.COLUMNS, columns);
    return this;
  }
  
  /**
   * Set columns. 
   * @param primaryKeyColumns Primary key columns for the table.
   * @return The builder instance for chained calls.
   */
  public TableBuilder primaryKey(String... primaryKeyColumns) {
    set(Param.PRIMARY_KEY, primaryKeyColumns.clone());
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
    return new Table
           (db,
            get(Param.NAME), 
            String.format("SELECT %s FROM %s", get(Param.COLUMNS), get(Param.NAME)));
  }

}

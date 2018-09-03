/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
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
 * Table builder.
 * 
 * 
 * @see Table
 * 
 * @since 1.0
 */
public final class TableBuilder  {
  /**
   * Constant for selecting all columns.
   */
  private static final String[] ALL_COLUMNS = { "*" };
  
  /**
   * Name.
   */
  private String tableName;
  
  /**
   * Columns.
   */
  private String[] tableColumns;
  
  /**
   * Columns.
   */
  private String[] keyColumns;

  /**
   * Constructs a new table builder.
   */
  public TableBuilder() {
    tableName = null;
    tableColumns = null;
  }
  
  /**
   * Set table name. 
   * @param name Name for the table.
   * @return The builder instance for chained calls.
   */
  public TableBuilder name(String name) {
    if (tableName != null) {
      throw new InvalidOperationException("The table name is already defined.");
    }
    tableName = name;
    return this;
  }
  
  /**
   * Set columns. 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  @SafeVarargs
  public final TableBuilder columns(String... columns) {
    if (columns == null || columns.length == 0) {
      throw new InvalidOperationException("Columns array is null or empty.");
    }
    if (tableColumns != null) {
      throw new InvalidOperationException("The table columns are already defined.");
    }
    tableColumns = columns.clone();
    return this;
  }
  
  /**
   * Set key columns.
   * 
   * <p>
   * Values for the key columns are used to uniquely identify database rows, e.g., they 
   * can be set to the columns that define the table's primary key. The key
   * columns are used by key-based operations like {@link JDBDT#delete(DataSet)}
   * and {@link JDBDT#update(DataSet)}.
   * 
   * @param columns Columns for the table.
   * @return The builder instance for chained calls.
   */
  @SafeVarargs
  public final TableBuilder key(String... columns) {
    if (columns == null || columns.length == 0) {
      throw new InvalidOperationException("Columns array is null or empty.");
    }
    if (keyColumns != null) {
      throw new InvalidOperationException("The key columns are already defined.");
    }
    keyColumns = columns.clone();
    return this;
  }
  /**
   * Build the table object.
   * @param db Database.
   * @return A new {@link Table} instance.
   */
  public Table build(DB db) {
    if (tableName == null) {
      throw new InvalidOperationException("The table name has not been set.");
    }
    if (tableColumns == null) {
      tableColumns = ALL_COLUMNS;
    }
    return new Table(CallInfo.create(), db, tableName, tableColumns, keyColumns); 
  }

}

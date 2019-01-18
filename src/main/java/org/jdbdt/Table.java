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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Table data source.
 * 
 * @see JDBDT#table(String)
 * @since 1.0
 */
public final class Table extends DataSource {
  /**
   * Table name.
   */
  private final String tableName;
  
  /**
   * Key columns.
   */
  private List<String> keyColumns;

  /**
   * Constructor.
   * @param db Database instance.
   * @param name Table name
   * @param columns Database columns.
   * @param keyColumns Key columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  public Table(DB db, String name, String[] columns, String[] keyColumns) {
    this(CallInfo.create(), db, name, columns, keyColumns);
  }
  
  /**
   * Constructor with supplied call info.
   * @param callInfo Call info.
   * @param db Database instance.
   * @param name Table name
   * @param columns Database columns.
   * @param keyCols Key columns.
   * @see JDBDT#table(String)
   * @see TableBuilder
   */
  Table(CallInfo callInfo, DB db, String name, String[] columns, String[] keyCols) {
    super(callInfo, db, 
          String.format("SELECT %s FROM %s", 
                         Misc.sqlArgumentList(columns), name));

    tableName = name;
    keyColumns = keyCols != null ? 
         Collections.unmodifiableList(Arrays.asList(keyCols))
       : Collections.emptyList();
  }

  /**
   * Get table name.
   * @return The name of the table.
   */
  public String getName() {
    return tableName;
  }

  /**
   * Get key columns.
   * @return Key columns.
   */
  public List<String> getKeyColumns() {
    return keyColumns;
  }
}

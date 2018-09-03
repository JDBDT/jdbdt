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

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of statement meta-data.
 * 
 * @since 1.0
 *
 */
final class MetaData {

  /**
   * Column information.
   * @since 1.0
   */
  static final class ColumnInfo {
    /**
     * Label. 
     */
    private final String label;
    
    /**
     * Type. 
     */
    private final JDBCType type;

    /**
     * Constructor.
     * @param label Label
     * @param type Type
     */
    ColumnInfo(String label, JDBCType type) {
      this.label = label;
      this.type = type;
    }
    
    /**
     * Get label.
     * @return The label for the column.
     */
    String label() {
      return label;
    }
    
    /**
     * Get type.
     * @return The JDBC type for the column.
     */
    JDBCType type() {
      return type;
    }

  }
  
  /**
   * Unmodifiable column info.
   */
  private final List<ColumnInfo> columns;

  /**
   * Constructs meta-data from given statement.
   * @param stmt Prepared statement for a query.
   * @throws SQLException If an error occurs during meta-data creation.
   */
  MetaData(PreparedStatement stmt) throws SQLException {

    ResultSetMetaData md = stmt.getMetaData();
    int n = md.getColumnCount();
    ArrayList<ColumnInfo> info = new ArrayList<>(n);
    for (int i = 1; i <= n; i++) {
      ColumnInfo ci = 
          new ColumnInfo(md.getColumnLabel(i),
              JDBCType.valueOf(md.getColumnType(i)));
      info.add(ci);
    }
    columns = Collections.unmodifiableList(info);
  }
  
  /**
   * Get column count.
   * @return The number of columns represented by this meta-data.
   */
  int getColumnCount() {
    return columns.size();
  }

  /**
   * Get column label.
   * @param index index of column, starting at 0.
   * @return Column label.
   */
  String getLabel(int index) {
    return columns.get(index).label();
  }

  /**
   * Get column type.
   * @param index index of column, starting at 0.
   * @return Column type.
   */
  JDBCType getType(int index) {
    return columns.get(index).type();
  }
  
  /**
   * Get column info. 
   * The list is unmodifiable (created through {@link Collections#unmodifiableList(List)}).
   * @return List of column info objects.
   */
  List<ColumnInfo> columns() {
    return columns;
  }
  
}

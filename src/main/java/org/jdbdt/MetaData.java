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
 * @since 0.1
 *
 */
final class MetaData {

  /**
   * Column information.
   * @since 0.1
   */
  static final class ColumnInfo {
    /** Label */
    private final String label;
    /** Type */
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
   */
  MetaData(PreparedStatement stmt) {
    try {
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
    } catch(SQLException e) {
      throw new UnexpectedDatabaseException(e);
    }
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

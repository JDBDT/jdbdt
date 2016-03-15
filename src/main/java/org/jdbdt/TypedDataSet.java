package org.jdbdt;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Typed data set.
 * @param <T> Type of objects.
 *
 * @since 0.1
 */
public class TypedDataSet<T> extends DataSet {

  /**
   * Conversion function.
   */
  private final Conversion<T> conv;
  
  /**
   * Construct a typed data set from a typed table.
   * @param tt Typed table.
   */
  TypedDataSet(TypedTable<T> tt) {
    this(tt, tt.conversion());
  }

  /**
   * Construct a typed data set from a typed query.
   * @param tq Typed query.
   */
  TypedDataSet(TypedQuery<T> tq) {
    this(tq, tq.conversion());
  }
  
  
  @SuppressWarnings("javadoc")
  private TypedDataSet(DataSource ds, Conversion<T> conv) {
    super(ds);
    this.conv = conv;
  }
  
  /**
   * Add row to the data set.
   * @param rowObj Row object.
   * @return The loader instance (for possible chaining).
   */
  public TypedDataSet<T> row(T rowObj) {
    super.row(conv.convert(rowObj));
    return this;
  }

  /**
   * Add rows to the data set.
   * @param rows Objects to map onto row values.
   * @return The loader instance (for possible chaining).
   * @throws SQLException If a database error occurs during insertion.
   */
  @SafeVarargs
  public final TypedDataSet<T> rows(T... rows) 
      throws SQLException {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }

  /**
   * Add rows in a collection to the data set.
   * @param rows Collection of row objects.
   * @return The loader instance (for possible chaining).
   * @throws SQLException If a database error occurs during insertion.
   */
  public TypedDataSet<T> rows(Collection<? extends T> rows) 
  throws SQLException {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }
}

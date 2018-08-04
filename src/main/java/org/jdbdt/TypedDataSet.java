package org.jdbdt;

import java.util.Collection;

/**
 * Typed data set.
 * @param <T> Type of objects.
 *
 * @see JDBDT#data(DataSource, Conversion)
 * @see Conversion
 * 
 * @since 0.1
 */
public final class TypedDataSet<T> extends DataSet {
  /**
   * Conversion function.
   */
  private final Conversion<T> conv;
  
  /**
   * Construct a typed data set.
   * @param ds Data source
   * @param conv Conversion function
   */
  public TypedDataSet(DataSource ds, Conversion<T> conv) {
    super(ds);
    this.conv = conv;
  }
  
  /**
   * Add row to the data set.
   * @param rowObj Row object.
   * @return The data set instance (for possible chaining).
   */
  public TypedDataSet<T> row(T rowObj) {
    super.row(conv.convert(rowObj));
    return this;
  }

  /**
   * Add rows to the data set.
   * @param rows Objects to map onto row values.
   * @return The data set instance (for possible chaining).
   */
  @SafeVarargs
  public final TypedDataSet<T> rows(T... rows)  {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }

  /**
   * Add rows in a collection to the data set.
   * @param rows Collection of row objects.
   * @return The data set instance (for possible chaining).
   */
  public TypedDataSet<T> rows(Collection<? extends T> rows) {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }
}

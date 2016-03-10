package org.jdbdt;

import java.util.Collection;
/**
 * Typed variant of a database delta.
 * 
 * @param <T> Type of object associated to this delta.
 *          
 * @since 0.1
 */
public final class TypedDelta<T> extends Delta {

  /**
   * Conversion function.
   */
  private final Conversion<T> conv;

  /**
   * Constructs a typed delta.
   * @param md Meta-data.
   * @param oldDS Old data set.
   * @param newDS New data set.
   * @param conv Conversion function.
   */
  TypedDelta(MetaData md, RowSet oldDS, RowSet newDS, Conversion<T> conv) {
    super(md, oldDS, newDS);
    this.conv = conv;
  }
  
  
  @SuppressWarnings("javadoc")
  private Object[] applyConv(T obj) {
    return conv.convert(obj);
  }
  
  /**
   * Assert that given row, encoded as an objects, is no longer defined.
   *
   * @param row Expected old row.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case one of the rows
   *         is still defined.
   */
  public TypedDelta<T> before(T row) throws DeltaAssertionError {
    super.before(applyConv(row));
    return this;
  }

  /**
   * Assert that given row, encoded as an object, is now defined.
   *
   * @param row Expected new row.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case one of the rows
   *         is still not defined          
   */
  public final TypedDelta<T> after(T row) throws DeltaAssertionError {
    super.after(applyConv(row));
    return this;
  }

  
  /**
   * Assert that a collection of rows is no longer defined.
   * 
   * @param rows Collection of objects defining rows.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case one of the rows
   *         is still defined   
   */
  public TypedDelta<T> before(Collection<? extends T> rows) 
  throws DeltaAssertionError {
    for (T r : rows) {
      super.before(applyConv(r));
    }
    return this;
  }

  /**
   * Assert that a collection of rows is now defined.
   * 
   * @param rows Collection of objects defining rows.
   * @return The delta object instance (for chained calls).
   * @throws DeltaAssertionError in case one of the rows
   *         is still not defined     
   */
  public TypedDelta<T> after(Collection<? extends T> rows) 
  throws DeltaAssertionError {
    for (T v : rows) {
      super.after(applyConv(v));
    }
    return this;
  }
  
  @Override
  public TypedDelta<T> before(Object... row) throws DeltaAssertionError {
    super.before(row);
    return this;
  }
  
  @Override
  public TypedDelta<T> after(Object... row) throws DeltaAssertionError {
    super.after(row);
    return this;
  }
  
  @Override
  public TypedDelta<T> before(DataSet ds)  throws DeltaAssertionError {
    super.before(ds);
    return this;
  }
  
  @Override
  public TypedDelta<T> after(DataSet ds)  throws DeltaAssertionError {
    super.after(ds);
    return this;
  }
  

}

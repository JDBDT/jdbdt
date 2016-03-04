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
   * Constructs a typed delta.
   * @param obs Observer instance.
   * @param oldDS Old data set.
   * @param newDS New data set.
   */
  TypedDelta(TypedObserver<T> obs, RowSet oldDS, RowSet newDS) {
    super(obs, oldDS, newDS);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public TypedObserver<T> getObserver() {
    return (TypedObserver<T>) super.getObserver();
  }
  
  @SuppressWarnings("javadoc")
  private Object[] applyConv(T obj) {
    return getObserver().conversion().convert(obj);
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

}

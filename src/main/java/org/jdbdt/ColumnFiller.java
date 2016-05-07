package org.jdbdt;

/**
 * Database column filler.
 *
 * <p>
 * If convenient, custom column fillers can be set for data set builders
 * using {@link DataSetBuilder#set(String, ColumnFiller)}.
 * The {@link DataSetBuilder} class already provides a number of
 * convenience methods that abstract the creation
 * of commonly used column fillers.
 * </p>
 * 
 * @param <T> Type of columns set by the filler.
 * @since 0.1
 */
public interface ColumnFiller<T> {
  /**
   * Get next value.
   * @return The next value to use for the database column.
   */
  T next();
}

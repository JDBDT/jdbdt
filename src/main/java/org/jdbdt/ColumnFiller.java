package org.jdbdt;

/**
 * Database column filler.
 *
 * <p>
 * When convenient, custom column fillers can be set for data set builders
 * using {@link DataSetBuilder#set(String, ColumnFiller)}.
 * Note that the {@link DataSetBuilder} class already provides various
 * convenience methods that abstract the creation
 * of commonly used column fillers.
 * </p>
 * 
 * @param <T> Type of columns set by the filler.
 * @since 1.0
 */
@FunctionalInterface
public interface ColumnFiller<T> {
  /**
   * Get next value.
   * @return The next value to use for the database column.
   */
  T next();
}

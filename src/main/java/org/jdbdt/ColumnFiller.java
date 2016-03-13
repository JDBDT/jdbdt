package org.jdbdt;

/**
 * Database column filler.
 *
 * <p>
 * Column fillers can be set directly for data sets
 * using {@link DataBuilder#set(String, ColumnFiller)} if desired.
 * Alternatively, the {@link DataBuilder} class provides many
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

package org.jdbdt;


/**
 * Conversion function from objects to database format.
 * 
 * 
 * <p>
 * The {@link #convert} method defines a mapping
 * of objects to database column values.
 * Given an object, the 
 * method should return an array  
 * of objects, such that
 * each array position defines a value for a database column.
 * The types of objects in the array must be compliant
 * with the JDBC constraints for mapping Java to SQL types
 * (<a href="https://jcp.org/en/jsr/detail?id=221">JDBC 4.x Specification</a>, 
 * Appendix B / "Data Type Conversion Tables"), and any
 * additional conventions for the database driver in use.
 * </p>
 * 
 * @param <T> Type of objects.
 * 
 * @since 0.1
 */
@FunctionalInterface
public interface Conversion<T> {
  /**
   * Convert object to row format. 
   * 
   * @param object Object.
   * @return Array of objects forming the columns of a database row. 
   */
  Object[] convert(T object);
}

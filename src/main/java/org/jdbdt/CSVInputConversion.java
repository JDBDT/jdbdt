package org.jdbdt;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.function.Function;


/**
 * CSV input conversion helper class.
 * @since 1.3
 *
 */
enum CSVInputConversion  {

  /** Singleton instance */
  INSTANCE;

  /**
   * Interface for data conversions.
   * @param <T> Type of data.
   */
  private class DataConversion<T> {
    /** Target lass */ 
    final Class<T> javaClass;
    /** Conversion function. */
    final Function<T, Object> func;

    @SuppressWarnings("javadoc")
    DataConversion(Class<T> javaClass, Function<T,Object> func) {
      this.javaClass = javaClass;
      this.func = func;
    }
  }


  @SuppressWarnings("javadoc")
  private final IdentityHashMap<JDBCType, LinkedList<DataConversion<?>>> dataConv 
  =  new IdentityHashMap<>();


  @SuppressWarnings("javadoc")
  private void init(JDBCType type, Class<?> javaClass) {
    init(type, javaClass, null);
  }

  @SuppressWarnings("javadoc")
  private <T> void init(JDBCType type, Class<T> javaClass, Function<T,Object> func) {
    LinkedList<DataConversion<?>> list = dataConv.get(type);
    if (list == null) {
      list = new LinkedList<>();
      dataConv.put(type, list);
    }
    list.addLast(new DataConversion<T>(javaClass, func));
  }

  /**
   * Convert object of given type.
   * @param type JDBC type.
   * @param o Input.
   * @return Converted object or <code>o</code> if no conversion is either possible or required. 
   */
  @SuppressWarnings("unchecked")
  public Object convert(JDBCType type, Object o) {
    Class<?> javaClass = o.getClass();
    LinkedList<DataConversion<?>> list = dataConv.get(type);
    if (list != null) {
      for (DataConversion<?> conv : list) {
        if (conv.javaClass == javaClass) {
          if (conv.func == null) {
            break;
          }
          else {
            try {
              o = ((Function<Object,Object>) conv.func).apply(o);
              break;
            }
            catch(RuntimeException e) {

            }
          }
        }
      }
    }
    return o;
  }



  /**
   * Constructor.
   */
  CSVInputConversion() {

    // BOOLEAN
    init(JDBCType.BOOLEAN, Boolean.class);
    init(JDBCType.BOOLEAN, Number.class, n -> n.intValue() != 0);
    init(JDBCType.BOOLEAN, String.class, Boolean::parseBoolean);
    init(JDBCType.BOOLEAN, String.class, s -> Integer.parseInt(s) != 0);

    // BIT
    init(JDBCType.BIT, Boolean.class);
    init(JDBCType.BIT, Number.class, n -> n.intValue() != 0);
    init(JDBCType.BIT, String.class, Boolean::parseBoolean);
    init(JDBCType.BIT, String.class, s -> Integer.parseInt(s) != 0);

    // TINYINT
    init(JDBCType.TINYINT, Byte.class);
    init(JDBCType.TINYINT, Number.class, Number::byteValue);
    init(JDBCType.TINYINT, String.class, Byte::parseByte);

    // SMALLINT
    init(JDBCType.SMALLINT, Short.class);
    init(JDBCType.SMALLINT, Number.class, Number::shortValue);
    init(JDBCType.SMALLINT, String.class, Short::parseShort);

    // INTEGER
    init(JDBCType.INTEGER, Integer.class);
    init(JDBCType.INTEGER, Number.class, Number::intValue);
    init(JDBCType.INTEGER, String.class, Integer::parseInt);

    // BIGINT
    init(JDBCType.BIGINT, Long.class);
    init(JDBCType.BIGINT, Number.class, Number::longValue);
    init(JDBCType.BIGINT, String.class, Long::parseLong);

    // REAL
    init(JDBCType.REAL, Float.class);
    init(JDBCType.REAL, Number.class, Number::floatValue);
    init(JDBCType.REAL, String.class, Float::parseFloat);

    // FLOAT
    init(JDBCType.FLOAT, Double.class);
    init(JDBCType.FLOAT, Number.class, Number::doubleValue);
    init(JDBCType.FLOAT, String.class, Double::parseDouble);

    // DOUBLE
    init(JDBCType.DOUBLE, Double.class);
    init(JDBCType.DOUBLE, Number.class, Number::doubleValue);
    init(JDBCType.DOUBLE, String.class, Double::parseDouble);

    // DECIMAL
    init(JDBCType.DECIMAL, BigDecimal.class);
    init(JDBCType.DECIMAL, Double.class, BigDecimal::valueOf);
    init(JDBCType.DECIMAL, Long.class, BigDecimal::valueOf);
    init(JDBCType.DECIMAL, String.class, s -> BigDecimal.valueOf(Double.parseDouble(s)));

    // NUMERIC
    init(JDBCType.NUMERIC, BigDecimal.class);
    init(JDBCType.NUMERIC, Double.class, BigDecimal::valueOf);
    init(JDBCType.NUMERIC, Long.class, BigDecimal::valueOf);
    init(JDBCType.NUMERIC, String.class, s -> BigDecimal.valueOf(Double.parseDouble(s)));

    // DATE
    init(JDBCType.DATE, java.sql.Date.class);
    init(JDBCType.DATE, String.class, java.sql.Date::valueOf);

    // TIME
    init(JDBCType.TIME, java.sql.Time.class);
    init(JDBCType.TIME, String.class, java.sql.Time::valueOf);

    // TIMESTAMP
    init(JDBCType.TIMESTAMP, java.sql.Timestamp.class);
    init(JDBCType.TIMESTAMP, String.class, java.sql.Timestamp::valueOf);

    // VARCHAR
    init(JDBCType.LONGNVARCHAR, String.class);
    init(JDBCType.VARCHAR, String.class);
    init(JDBCType.CHAR, String.class);
  }
}


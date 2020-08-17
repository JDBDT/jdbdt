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
   * Functional interface for string conversion.
   */
  @FunctionalInterface
  private interface StringConversion {
    /**
     * Convert a string onto an object.
     * @param s String.
     * @return An object.
     * @throws IllegalArgumentException If the string format is invalid.
     */
    Object convert(String s) throws IllegalArgumentException;
  }
  


  @SuppressWarnings("javadoc")
  private final IdentityHashMap<JDBCType, LinkedList<StringConversion>> dataConv 
  =  new IdentityHashMap<>();



  @SuppressWarnings("javadoc")
  private <T> void init(JDBCType type, Class<T> javaClass, Function<String,T> func) {
    LinkedList<StringConversion> list = dataConv.get(type);
    if (list == null) {
      list = new LinkedList<>();
      dataConv.put(type, list);
    }
    list.addLast(s -> func.apply(s));
  }

  /**
   * Convert string.
   * @param type JDBC type.
   * @param s Input.
   * @return Converted object or <code>o</code> if no conversion is either possible or required. 
   */
  public Object convert(JDBCType type, String s) {
    LinkedList<StringConversion> list = dataConv.get(type);
    Object o = s;
    if (list != null) {
      for (StringConversion conv : list) {
        try {
          o = conv.convert(s);
          break;
        }
        catch(IllegalArgumentException e) {
          // ignore deliberately
        }
        catch(RuntimeException e) {
          throw new CSV.InvalidConversionException(s, e);
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
    init(JDBCType.BOOLEAN, Boolean.class, s -> Integer.parseInt(s) != 0);
    init(JDBCType.BOOLEAN, Boolean.class, Boolean::parseBoolean);

    // BIT
    init(JDBCType.BIT, Boolean.class, s -> Integer.parseInt(s) != 0);
    init(JDBCType.BIT, Boolean.class, Boolean::parseBoolean);

    // TINYINT
    init(JDBCType.TINYINT, Byte.class, Byte::parseByte);

    // SMALLINT
    init(JDBCType.SMALLINT, Short.class, Short::parseShort);

    // INTEGER
    init(JDBCType.INTEGER, Integer.class, Integer::parseInt);

    // BIGINT
    init(JDBCType.BIGINT, Long.class, Long::parseLong);

    // REAL
    init(JDBCType.REAL, Float.class, Float::parseFloat);

    // FLOAT
    init(JDBCType.FLOAT, Double.class, Double::parseDouble);

    // DOUBLE
    init(JDBCType.DOUBLE, Double.class, Double::parseDouble);

    // DECIMAL
    init(JDBCType.DECIMAL, BigDecimal.class, s -> BigDecimal.valueOf(Double.parseDouble(s)));

    // NUMERIC
    init(JDBCType.NUMERIC, BigDecimal.class, s -> BigDecimal.valueOf(Double.parseDouble(s)));

    // DATE
    init(JDBCType.DATE, java.sql.Date.class, java.sql.Date::valueOf);

    // TIME
    init(JDBCType.TIME, java.sql.Time.class, java.sql.Time::valueOf);

    // TIMESTAMP
    init(JDBCType.TIMESTAMP, java.sql.Timestamp.class, java.sql.Timestamp::valueOf);

//    // VARCHAR, LONGNVARCHAR, CHAR
//    init(JDBCType.LONGNVARCHAR, String.class, s -> s);
//    init(JDBCType.VARCHAR, String.class, s -> s);
//    init(JDBCType.CHAR, String.class, s -> s);
  }
}


/*
 * The MIT License
 *
 * Copyright (c) Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jdbdt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.function.Function;


/**
 * Support for reading/writing data sets from/to CSV format.
 * 
 * @since 1.3
 */
public final class CSV {

  /**
   * Line separator setting.
   * @see Format#lineSeparator(LineSeparator)
   * @see BufferedWriter#newLine()
   * @see BufferedReader#readLine()
   * @since 1.3
   */
  public enum LineSeparator {
    /** 
     * System default (<code>System.get("line.separator")</code> 
     */
    SYSTEM_DEFAULT(System.getProperty("line.separator")),
    /**
     * Carriage return (<code>\r\n</code>).
     */
    CR("\r"),
    /** 
     * Line feed. (<code>\n</code>). 
     */
    LF("\n"),
    /** 
     * Carriage return and line feed (<code>\r\n</code>).
     */
    CR_LF("\r\n");

    /** Line separator string. */
    private final String separator;

    /**
     * Constructor.
     * @param separator Separator.
     */
    LineSeparator(String separator) {
      this.separator = separator; 
    }

    /**
     * Get separator.
     * @return The line separator string.
     */
    public String separator() {
      return separator;
    }
  }

  /**
   * CSV format specification.
   */
  public static final class Format implements Cloneable { 
    /** Separator. */
    private String separator = ",";
    /** Comment sequence. */
    private String lineCommentSequence = "";
    /** Line separator. */
    private LineSeparator lineSeparator = LineSeparator.SYSTEM_DEFAULT;
    /** Header flag. */
    private boolean header = false;
    /** Null value string. */
    private String nullValue = "";
    /** Read conversions flag. */
    private boolean useReadConversions = false;

    /** Constructor. Default values will be set. */
    public Format() { }

    /**
     * Set separator.
     * By default the separator is formed by the comma character alone (<code>","</code>).
     * @param s Separator string (usually formed by just one character).
     * @return The object instance (to facilitate chained calls).
     */
    public Format separator(String s) {
      validateSequence(s);
      separator = s;
      return this;
    }

    /**
     * Set line comment sequence.
     * Lines starting with the specified sequence will be ignored.
     * By default no comment sequence is set.
     * @param s Line comment sequence.
     * @return The object instance (to facilitate chained calls).
     */
    public Format lineComment(String s) {
      validateSequence(s);
      lineCommentSequence = s;
      return this;
    }

    /**
     * Set line separator.
     * By default, the line separator is {@link LineSeparator#SYSTEM_DEFAULT}. 
     * @param ls Line separator to use.
     * @return The object instance (to facilitate chained calls).
     */
    public Format lineSeparator(LineSeparator ls) {
      lineSeparator = ls;
      return this;
    }

    /**
     * Set string for representing NULL values. 
     * By default the empty string is used for NULL values.
     * @param s Line comment sequence  (usually formed by just one character).
     * @return The object instance (to facilitate chained calls).
     */
    public Format nullValue(String s) {
      validateSequence(s);
      nullValue = s;
      return this;
    }

    /**
     * Indicates that CSV file has header.
     * @return The object instance (to facilitate chained calls).
     */
    public Format hasHeader() {
      header = true;
      return this;
    }

    /**
     * Indicates that string conversions should be used in 
     * conjunction with {@link JDBDT#read(DataSource, Format, File)}.
     * 
     * @return The object instance (to facilitate chained calls).
     */
    public Format useReadConversions() {
      useReadConversions = true;
      return this;
    }

    @Override
    public Format clone() {
      try {
        return (Format) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalErrorException(e);
      }
    }

    @SuppressWarnings("javadoc")
    private void validateSequence(String s) {
      if (s == null || s.length() == 0) {
        throw new InvalidOperationException("Argument must be a non-empty string.");
      }   
    }
  }

  /**
   * Read data set from CSV file.
   * 
   * <p>
   * If the header option is set for the CSV format specification, the 
   * first line is skipped.
   * </p>
   * 
   * @param callInfo Call info.
   * @param source Data source.
   * @param format CSV format specification.
   * @param file File.
   * @return Data set read from file.
   * @throws InputOutputException if an I/O error occurs.
   */
  static DataSet 
  read(CallInfo callInfo, DataSource source, Format format, File file) throws InputOutputException {
    try(BufferedReader in = new BufferedReader(new FileReader(file))) {
      int lineCount = 1;
      format = format.clone();
      if (format.header) {
        in.readLine();
        lineCount ++;
      }
      final DataSet dataSet = new DataSet(source);
      final int columnCount = source.getColumnCount();
      final MetaData md = source.getMetaData();

      final boolean commentSeqDefined = format.lineCommentSequence.length() > 0;
      String line;
      while ((line = in.readLine()) != null) {
        lineCount++;
        if (commentSeqDefined && line.startsWith(format.lineCommentSequence)) {
          continue;
        }
        String[] values = line.split(format.separator, columnCount);
        if (values.length != columnCount) {
          throw new InvalidCSVConversionException("Invalid number of values at line " + lineCount + ".");
        }
        Object[] data = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
          String text = values[i];
          Object object;
          if (text.equals(format.nullValue)) {
            object = null; 
          }
          else if (format.useReadConversions) {
            object = Conversions.INSTANCE.convert(md.getType(i), text);
          } 
          else {
            object = text;
          }
          data[i] = object;
        }
        dataSet.addRow(new Row(data));
      }
      source.getDB().logDataSetOperation(callInfo, dataSet);
      return dataSet;
    }
    catch (IOException e) {
      throw new InputOutputException(e);
    }
  }


  /**
   * Write data set to CSV file.
   * 
   * <p>
   * If the header option is set for the CSV format specification, column
   * names will be written in the first line of the output file.
   * </p>
   * 
   * @param callInfo callInfo
   * @param dataSet Data set.
   * @param format CSV format specification.
   * @param file Output file.
   * @throws InputOutputException if an I/O error occurs.
   */
  static void 
  write(CallInfo callInfo, DataSet dataSet, Format format, File file) throws InputOutputException {
    try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
      final DataSource source = dataSet.getSource();
      final int colCount = source.getColumnCount(); 
      final String eol = format.lineSeparator.separator();
      format = format.clone();
      if (format.lineCommentSequence.length() > 0) {
        out.write(format.lineCommentSequence);
        out.write(" CSV data file generated using JDBDT ");
        out.write(VersionInfo.ID); 
        out.write(eol);
      }
      if (format.header) {
        out.write(source.getColumnName(0));
        for (int i = 1; i < colCount; i++) {
          out.write(format.separator);
          out.write(source.getColumnName(i));
        }
        out.write(eol);
      }
      for (Row r : dataSet.getRows()) {
        Object[] values = r.data();
        writeValue(out, format, values[0]);
        for (int i = 1; i < colCount; i++) {
          out.write(format.separator);
          writeValue(out, format, values[i]);
        }
        out.write(eol);
      }
    }
    catch (IOException e) {
      throw new InputOutputException(e);
    }
    dataSet.getSource().getDB().logDataSetOperation(callInfo, dataSet);
  }

  /**
   * CSV input conversion helper class.
   */
  private enum Conversions  {

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
      list.addLast(func::apply);
    }

    /**
     * Convert string.
     * @param type JDBC type.
     * @param text Input.
     * @return Converted object or <code>o</code> if no conversion is either possible or required. 
     */
    public Object convert(JDBCType type, String text) {
      LinkedList<StringConversion> list = dataConv.get(type);
      Object object = text;
      if (list != null) {
        for (StringConversion conv : list) {
          try {
            object = conv.convert(text);
            break;
          }
          catch(IllegalArgumentException e) {
            // ignore deliberately
          }
          catch(RuntimeException e) {
            throw new InvalidCSVConversionException(text, e);
          }
        }
      }
      return object;
    }


    /**
     * Constructor.
     */
    Conversions() {

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

    }
  }

  
  @SuppressWarnings("javadoc")
  private static void 
  writeValue(BufferedWriter out, Format format, Object value) throws IOException {
    out.write(value == null ? format.nullValue : value.toString());
  }


  /**
   * Private constructor to prevent instantiation.
   */
  private CSV() {
    
  }
}

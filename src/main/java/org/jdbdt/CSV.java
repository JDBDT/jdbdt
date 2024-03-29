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
 * @see JDBDT#read(DataSource,CSV.Format,java.io.File)
 * @see JDBDT#write(DataSet,CSV.Format,java.io.File)
 * 
 * @since 1.3
 */
public final class CSV {

  /**
   * Line separator setting.
   * @see CSV.Format#lineSeparator(LineSeparator)
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
     * Carriage return (<code>'\r'</code>).
     */
    CR("\r"),
    /** 
     * Line feed. (<code>'\n'</code>). 
     */
    LF("\n"),
    /** 
     * Carriage return and line feed (<code>'\r\n'</code>).
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
   * @since 1.3
   */
  public static final class Format implements Cloneable { 
    /** Separator. */
    private char separator = ',';
    /** Quote character. */
    private char escapeCh = '"';
    /** Comment sequence character. */
    private char lineCommentChar = 0;
    /** Line separator. */
    private LineSeparator lineSeparator = LineSeparator.SYSTEM_DEFAULT;
    /** Header flag. */
    private boolean header = false;
    /** Null value string. */
    private String nullValue = "";
    /** Read conversions object. */
    private Conversions readConversions = null;
    /** Always escape during ouput. */
    private boolean alwaysEscapeOutput = false;

    /** Constructor. Default values will be set. */
    public Format() { }

    /**
     * Set separator.
     * By default the separator is formed by the comma character alone (<code>","</code>).
     * @param s Separator string (usually formed by just one character).
     * @return The object instance (to facilitate chained calls).
     * @deprecated As of release 1.4, replaced by {@link #separator(char)}.
     */
    @Deprecated
    public Format separator(String s) {
      if (s == null || s.length() != 1) {
        throw new InvalidOperationException("Separator can only contain one character.");
      }
      return separator(s.charAt(0));
    }

    /**
     * Set separator.
     * By default the separator is the comma character (<code>','</code>).
     * @param s Separator character to use.
     * @return The object instance (to facilitate chained calls).
     * @since 1.4
     */
    public Format separator(char s) {
      separator = s;
      return this;
    }

    /**
     * Set escape character.
     * <p>
     * By default the separator is the double quote character (<code>"</code>).
     * Note that escaping follows the <a href="https://www.ietf.org/rfc/rfc4180.txt" target="_top">RFC-4180</a> 
     * convention, except for the possibility of having line breaks and carriage returns within
     * escaped sequences.
     * </p>
     * @param e Character to use as escape character.
     * @return The object instance (to facilitate chained calls).
     */
    public Format escape(char e) {
      escapeCh = e;
      return this;
    }

    /**
     * Set line comment character.
     * Lines starting with the specified character will be ignored.
     * By default no comment character is set.
     * @param lcs Line comment sequence (should contain one character, call has been deprecated).
     * @return The object instance (to facilitate chained calls).
     * @deprecated As of release 1.4, replaced by {@link #lineComment(char)}.
     */
    @Deprecated
    public Format lineComment(String lcs) {
      if (lcs == null || lcs.length() != 1) {
        throw new InvalidOperationException("Line comment sequence is defined by one character.");
      }
      lineComment(lcs.charAt(0));
      return this;
    }
    
    /**
     * Set line comment character.
     * Lines starting with the specified character will be ignored.
     * By default no comment character is set.
     * @param lcc Line comment character.
     * @return The object instance (to facilitate chained calls).
     */
    public Format lineComment(char lcc) {
      lineCommentChar = lcc;
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
     * conjunction with {@link JDBDT#read(DataSource,CSV.Format,File)}.
     * 
     * <p>
     * Standard built-in conversions will be used, which may be
     * overridden for specific JDBC types with {@link CSV.Format#overrideConversion}
     * </p>
     * 
     * <p>
     * If this option is not set, then data rows read from a CSV file 
     * will consist only of values of type {@link java.lang.String}, without
     * any conversion being performed.  
     * </p>
     * 
     * @return The object instance (to facilitate chained calls).
     */
    public Format useReadConversions() {
      readConversions = new Conversions();
      return this;
    }
    
    /**
     * Override input conversion for given JDBC type.
     * 
     * @param type JDBC type.
     * @param conv Conversion function.
     * @return The object instance (to facilitate chained calls).
     * @see #useReadConversions()
     * @since 1.4.1
     */
    public Format overrideConversion(JDBCType type, Function<String,?> conv) {
      if (readConversions == null) {
        throw new InvalidOperationException("Read conversions are not active!");
      }
      if (conv == null) {
        throw new InvalidOperationException("Null conversion function!");
      }
      readConversions.set(type, Object.class, conv::apply);
      return this;
    }

    /**
     * Indicates that the escape sequence should always be
     * used when writing values. 
     * 
     * If not set (the default) escape sequences will be 
     * written only when necessary.
     * 
     * @return The object instance (to facilitate chained calls).
     * @since 1.4
     */
    public Format alwaysEscapeOutput() {
      alwaysEscapeOutput = true;
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

    /**
     * Parse a CSV file.
     * @param source Data Source.
     * @param file Input file.
     * @return Parsed data set.
     * @throws InputOutputException if an I/O error occurs.
     */
    DataSet read(DataSource source, File file) {
      try(BufferedReader in = new BufferedReader(new FileReader(file))) {
        int lineCount = 1;
        if (header) {
          in.readLine();
          lineCount ++;
        }
        final DataSet dataSet = new DataSet(source);
        final int columnCount = source.getColumnCount();
        final MetaData md = source.getMetaData();

        String line;
        while ((line = in.readLine()) != null) {
          lineCount++;
          if (lineCommentChar != 0 && line.charAt(0) == lineCommentChar) {
            continue;
          }
          Object[] data = new Object[columnCount];
          if (! read(line, data)) {
            throw new InvalidCSVConversionException("Invalid input at line " + lineCount + ".");
          }
          for (int i = 0; i < columnCount; i++) {
            String text = (String) data[i];
            if (text.equals(nullValue)) {
              data[i] = null; 
            }
            else if (readConversions != null) {
              data[i] = readConversions.convert(md.getType(i), text);
            } 
          }
          dataSet.addRow(new Row(data));
        }
        return dataSet;
      }
      catch (IOException e) {
        throw new InputOutputException(e);
      }
    }
    
    /**
     * Parse a single CSV line.
     * @param line The line to parse.
     * @param fields CSV field array.
     * @return {@code true} if all fields could be parsed (not more, not fewer).
     */
    boolean read(String line, Object[] fields) {
      int fieldCount = 0;
      int len = line.length();
      int state = 0; // begin fields
      StringBuilder out = new StringBuilder();
      for (int pos = 0; pos < len; pos++) {
        char c = line.charAt(pos);
        switch (state) {
          case 0:
            if (c == escapeCh) {
              state = 2;
            } else if (c != separator) {
              out.append(c);
              state = 1;
            }
            break;
          case 1:
            if (c == separator) {
              state = 0;
            } else {
              out.append(c);
            }
            break;
          case 2:
            if (c == escapeCh) {
              state = 3;
            } else { 
              out.append(c);
            }
            break;
          case 3:
            if (c == separator) {
              state = 0;
            } else if (c == escapeCh){
              out.append(c);
              state = 2;
            } else {
              return false;
            }
            break;
          default:
            throw new InternalErrorException();
        }
        if (state == 0) {
          if (fieldCount == fields.length) {
            return false; // too many fields
          }
          fields[fieldCount] = out.toString();
          out = new StringBuilder();
          fieldCount++;
        }
      }
      if (fieldCount == fields.length || state == 2) {
        return false;
      }
      fields[fieldCount] = out.toString();
      fieldCount++;
      return fieldCount == fields.length;
    }

    /**
     * Write dataset to CSV file.
     * @param dataSet Data set.
     * @param file Output file.
     * @throws InputOutputException if an I/O error occurs.
     */
    void write(DataSet dataSet, File file) {
      try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
        final DataSource source = dataSet.getSource();
        final int colCount = source.getColumnCount(); 
        final String eol =  lineSeparator.separator();
        if (lineCommentChar != 0) {
          out.write(lineCommentChar);
          out.write(" CSV data file generated using JDBDT ");
          out.write(VersionInfo.ID); 
          out.write(eol);
        }
        if (header) {
          out.write(source.getColumnName(0));
          for (int i = 1; i < colCount; i++) {
            out.write(separator);
            out.write(source.getColumnName(i));
          }
          out.write(eol);
        }
        for (Row r : dataSet.getRows()) {
          Object[] values = r.data();
          write(out, values[0]);
          for (int i = 1; i < colCount; i++) {
            out.write(separator);
            write(out, values[i]);
          }
          out.write(eol);
        }
      }
      catch (IOException e) {
        throw new InputOutputException(e);
      }
    }
    /**
     * Write value.
     * @param out Output stream.
     * @param value Value to write.
     */
    void write(BufferedWriter out, Object value) {
      try {
        out.write(toOutputFormat(value));
      } 
      catch(IOException e) {
        throw new InputOutputException(e);
      }
    }
    
    /**
     * Convert to output format.
     * @param value Value to convert.
     * @return String in output format.
     */
    private String toOutputFormat(Object value) {
      if (value == null) {
        return nullValue;
      } 
      final String str = value.toString();
      final int n = str.length();
      final StringBuilder out = new StringBuilder();
      boolean escape = alwaysEscapeOutput;
      for (int i = 0; i < n; i++) {
        char c = str.charAt(i);
        if (c == escapeCh) {
          escape = true;
          out.append(escapeCh);
        } else  if (c == separator || c == '\r' || c == '\n') {
          escape = true;
        }
        out.append(c);
      }
      if (escape) {
        out.insert(0, escapeCh);
        out.append(escapeCh);
      }
      return out.toString();
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
    DataSet d = format.read(source, file);
    source.getDB().logDataSetOperation(callInfo, d);
    return d;
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
    format.write(dataSet, file);
    dataSet.getSource().getDB().logDataSetOperation(callInfo, dataSet);
  }

  /**
   * CSV input conversion helper class.
   */
  private static class Conversions  {

    /**
     * Functional interface for string conversion.
     */
    @FunctionalInterface
    private interface SC {
      /**
       * Convert a string onto an object.
       * @param s String.
       * @return An object.
       * @throws IllegalArgumentException If the string format is invalid.
       */
      Object convert(String s) throws IllegalArgumentException;
    }



    @SuppressWarnings("javadoc")
    private final IdentityHashMap<JDBCType, LinkedList<SC>> dataConv 
    =  new IdentityHashMap<>();



    
    /**
     * Set conversion.
     * @param <T> Type of objects
     * @param type JDBC type.
     * @param javaClass Java class.
     * @param func Function.
     */
    <T> void set(JDBCType type, Class<T> javaClass, Function<String,T> func) {
      LinkedList<SC> list = dataConv.get(type);
      if (list == null) {
        list = new LinkedList<>();
        dataConv.put(type, list);
      }
      list.addFirst(func::apply);
    }
    
    /**
     * Convert string.
     * @param type JDBC type.
     * @param text Input.
     * @return Converted object or <code>o</code> if no conversion is either possible or required. 
     */
    public Object convert(JDBCType type, String text) {
      LinkedList<SC> list = dataConv.get(type);
      Object object = text;
      if (list != null) {
        for (SC conv : list) {
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
      set(JDBCType.BOOLEAN, Boolean.class, s -> Integer.parseInt(s) != 0);
      set(JDBCType.BOOLEAN, Boolean.class, Boolean::parseBoolean);

      // BIT
      set(JDBCType.BIT, Boolean.class, s -> Integer.parseInt(s) != 0);
      set(JDBCType.BIT, Boolean.class, Boolean::parseBoolean);

      // TINYINT
      set(JDBCType.TINYINT, Byte.class, Byte::parseByte);

      // SMALLINT
      set(JDBCType.SMALLINT, Short.class, Short::parseShort);

      // INTEGER
      set(JDBCType.INTEGER, Integer.class, Integer::parseInt);

      // BIGINT
      set(JDBCType.BIGINT, Long.class, Long::parseLong);

      // REAL
      set(JDBCType.REAL, Float.class, Float::parseFloat);

      // FLOAT
      set(JDBCType.FLOAT, Double.class, Double::parseDouble);

      // DOUBLE
      set(JDBCType.DOUBLE, Double.class, Double::parseDouble);

      // DECIMAL
      set(JDBCType.DECIMAL, BigDecimal.class, s -> new BigDecimal(s));

      // NUMERIC
      set(JDBCType.NUMERIC, BigDecimal.class, s -> new BigDecimal(s));

      // DATE
      set(JDBCType.DATE, java.sql.Date.class, java.sql.Date::valueOf);

      // TIME
      set(JDBCType.TIME, java.sql.Time.class, java.sql.Time::valueOf);

      // TIMESTAMP
      set(JDBCType.TIMESTAMP, java.sql.Timestamp.class, java.sql.Timestamp::valueOf);

    }
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private CSV() {

  }
}

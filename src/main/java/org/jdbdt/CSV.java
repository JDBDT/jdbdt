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
   * @since 1.3
   */
  public static final class Format implements Cloneable { 
    /** Separator. */
    private char separator = ',';
    /** Quote character. */
    private char escapeCh = '"';
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
     * By default the separator is the double quote character (<code>"</code>).
     * @param e Character to use as espace character.
     * @return The object instance (to facilitate chained calls).
     */
    public Format escape(char e) {
      escapeCh = e;
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

        final boolean commentSeqDefined = lineCommentSequence.length() > 0;
        String line;
        while ((line = in.readLine()) != null) {
          lineCount++;
          if (commentSeqDefined && line.startsWith(lineCommentSequence)) {
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
            else if (useReadConversions) {
              data[i] = Conversions.INSTANCE.convert(md.getType(i), text);
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
      //System.out.println(fieldCount + " " + beg + " " + end + " \"" + fields[fieldCount] + "\"");
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
        if (lineCommentSequence.length() > 0) {
          out.write(lineCommentSequence);
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
        if (value == null) {
          out.write(nullValue);
        } else {
          String str = value.toString();
          if (alwaysEscapeOutput || str.indexOf(separator) != -1) {
            out.write(escapeCh);
            out.write(str);
            out.write(escapeCh);
          } else {
            out.write(str);
          }
        }
      } 
      catch(IOException e) {
        throw new InputOutputException(e);
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

  /**
   * Private constructor to prevent instantiation.
   */
  private CSV() {

  }
}

package org.jdbdt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility methods to read / write data sets onto CSV format.
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
    
    
    /**
     * Line separator string.
     */
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
     * @param s Line comment sequence .
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
   * @param source Data source.
   * @param format CSV format specification.
   * @param file File.
   * @return Data set read from file.
   * @throws IOException if an I/O error occurs.
   */
  public static DataSet 
  read(DataSource source, Format format, File file) throws IOException {
    try(BufferedReader in = new BufferedReader(new FileReader(file))) {
      int lineCount = 1;
      format = format.clone();
      if (format.header) {
        in.readLine();
        lineCount ++;
      }
      final DataSet dataSet = new DataSet(source);
      final int columnCount = source.getColumnCount();
      final boolean commentSeqDefined = format.lineCommentSequence.length() > 0;
      String line;
      while ((line = in.readLine()) != null) {
        lineCount++;
        if (commentSeqDefined && line.startsWith(format.lineCommentSequence)) {
          continue;
        }

        String[] values = line.split(format.separator, columnCount);
        if (values.length != columnCount) {
          throw new IOException("Invalid number of values at line " + lineCount + ".");
        }
        Object[] data = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
          data[i] = parseValue(format, values[i]);
        }
        dataSet.addRow(new Row(data));
      }
      return dataSet;
    }
  }

  @SuppressWarnings("javadoc")
  private static String 
  parseValue(Format format, String value) {
    return value.equals(format.nullValue) ? null : value;
  }

  /**
   * Write data set to CSV file.
   * 
   * <p>
   * If the header option is set for the CSV format specification, column
   * names will be written in the first line of the output file.
   * </p>
   * 
   * @param dataSet Data set.
   * @param format CSV format specification.
   * @param file Output file.
   * @throws IOException if an I/O error occurs.
   */
  public static void 
  write(DataSet dataSet, Format format, File file) throws IOException {
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
  }
  
  @SuppressWarnings("javadoc")
  private static void 
  writeValue(BufferedWriter out, Format format, Object value) throws IOException {
    out.write(value == null ? format.nullValue : value.toString());
  }
  
  
}



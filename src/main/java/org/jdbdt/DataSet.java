package org.jdbdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data set.
 * 
 * <p>A data set represents a collection of database rows.</p>
 * 
 * @since 0.1
 *
 */
public class DataSet {
  
  /**
   * Data source.
   */
  private final DataSource source;

  /**
   * Rows in the data set.
   */
  private final ArrayList<Row> rows;

  /**
   * Read-only flag.
   */
  private boolean readOnly;

  /**
   * Constructs a new data set.
   * @param ds Data source.
   */
  DataSet(DataSource ds) {
    this(ds, new ArrayList<>());
  }

  /**
   * Constructs a new row set.
   * @param ds Data source.
   * @param list Row list. 
   */
  DataSet(DataSource ds, ArrayList<Row> list) {
    this.source = ds;
    this.rows = list;
  }
  
  /**
   * Check if the data set is read-only.
   * 
   * <p>
   * Adding rows to a read-only data set will result in an exception 
   * of type {@link InvalidOperationException}.
   * </p>
   * 
   * @see #setReadOnly()
   * @return <code>true</code> if data set is read-only.
   */
  public final boolean isReadOnly() {
    return readOnly;
  }
  
  /**
   * Set data set as read-only.
   * 
   * <p>
   * Adding rows to a read-only data set will result in an exception 
   * of type {@link InvalidOperationException}.
   * The read-only setting cannot be disabled after this method
   * is called.
   * </p>
   * 
   * @see #isReadOnly()
   * 
   */
  public final void setReadOnly() {
    readOnly = true;
  }

  /**
   * Get data source.
   * @return The data source associated to this 
   *   data set.
   */
  public final DataSource getSource() {
    return source;
  }

  /**
   * Create a builder for this data set.
   * @return A new builder for this data set.
   */
  public final DataSetBuilder build() {
    return new DataSetBuilder(this);
  }
  
  /**
   * Check if the data set is empty.
   * @return <code>true</code> is the data set has no rows.
   */
  public final boolean isEmpty() {
    return rows.isEmpty();
  }

  /**
   * Get size of data set.
   * @return The number of rows in the set.
   */
  public final int size() {
    return rows.size();
  }

  /**
   * Get internal list of rows (package access only)
   * @return Internal list of rows in the data set.
   */
  final List<Row> getRows() {
    return rows;
  }

  /**
   * Add a row to the data set.
   * @param columnValues Column values forming a row. 
   * @return The data set instance (for chained calls).
   * @throws InvalidOperationException if this data set is read-only.
   * @see #rows(Object[][])
   * @see #add(DataSet)
   * @see #isReadOnly()
   */
  @SafeVarargs
  public final DataSet row(Object... columnValues) {
    checkIfNotReadOnly();
    if (columnValues.length != source.getColumnCount()) {
      throw new InvalidOperationException(source.getColumnCount() +
          " columns expected, not " + columnValues.length + ".");
    }
    addRow(new Row(columnValues));
    return this;
  }


  /**
   * Add rows to the data set.
   * @param rows Array of rows. 
   * @return The data set instance (for chained calls).
   * @throws InvalidOperationException if this data set is read-only.
   * @see #row(Object...)
   * @see #add(DataSet)
   * @see #isReadOnly()
   */
  public final DataSet rows(Object[][] rows) {
    checkIfNotReadOnly();
    for (Object[] columnValues : rows) {
      addRow(new Row(columnValues));
    }
    return this;
  }
  
  /**
   * Add rows of given data set to this data set.
   * 
   * @param other The other data set.
   * @return The data set instance (for chained calls).
   * @throws InvalidOperationException 
   *   if this data set is read-only, or 
   *   if the data source of the other data set differs
   *   from this one.
   * @see #row(Object...)
   * @see #rows(Object[][])
   * @see #isReadOnly()
   */
  public final DataSet add(DataSet other) {
    checkIfNotReadOnly();
    if (other.getSource() != source) {
      throw new InvalidOperationException("Data source mismatch.");
    }
    rows.addAll(other.rows);
    return this;
  }

  /**
   * Add a row to the set (package-private version; ignores read-only setting).
   * @param r Row to add.
   */
  final void addRow(Row r) {
    rows.add(r);
  }

  /**
   * Create a subset of the rows of given data set.
   * @param data Source data set.
   * @param startIndex Start index (from 0 to <code>data.size()-1</code>).
   * @param n Number of rows.
   * @return A new data set containing 
   *        the rows in the specified range.
   * @throws InvalidOperationException if the given data
   * set is null or if the specified column range is invalid.
   */
  public static DataSet subset(DataSet data, int startIndex, int n) 
  throws InvalidOperationException {
    if (data == null) {
      throw new InvalidOperationException("Null data set");
    }
    if (startIndex < 0 || n < 0 || startIndex + n > data.size()) {
      throw new InvalidOperationException("Invalid range.");
    }
    final DataSet sub = new DataSet(data.getSource());
    final int endIndex = startIndex + n;
    for (int i = startIndex; i < endIndex; i++) {
      sub.rows.add(data.rows.get(i));
    }
    return sub;
  }
  
  /**
   * Create a data set with only one row from the given
   * data set.
   * @param data Source data set.
   * @param index Row index (from 0 to <code>data.size()-1</code>).
   * @return A new data set containing the <code>index</code>-th 
   * row of the source data set.
   * @throws InvalidOperationException if the given data
   * set is null or if the specified column index is invalid.
   */
  public static DataSet singleton(DataSet data, int index)
  throws InvalidOperationException {
    return subset(data, index, 1);
  }
  
  /**
   * Create sub-set with the first <code>n</code> rows.
   * @param data Source data set.
   * @param n Number of rows.
   * @return A new data set containing the first 
   * <code>n</code> rows in the source data set.
   * @throws InvalidOperationException if the given data
   * set is null or if the value of <code>n</code> is invalid.
   */
  public static DataSet first(DataSet data, int n) 
  throws InvalidOperationException {
    return subset(data, 0, n);
  }
  
  /**
   * Create sub-set with the last <code>n</code> rows.
   * @param data Source data set.
   * @param n Number of rows.
   * @return A new data set containing the last 
   *        <code>n</code> rows in the source data set.
   * @throws InvalidOperationException if the given data
   * set is null or if the value of <code>n</code> is invalid.
   */
  public static DataSet last(DataSet data, int n)
  throws InvalidOperationException {
    return subset(data, data.size() - n, n);
  }
  
  /**
   * Create data set with the same contents of given data set.
   * @param data Source data set.
   * @return A new data set containing all
   *        rows in the source data set.
   * @throws InvalidOperationException if given data set is null.
   */
  public static DataSet copyOf(DataSet data) 
  throws InvalidOperationException {
    if (data == null) {
      throw new InvalidOperationException("Null data set");
    }
    DataSet r = new DataSet(data.getSource());
    r.getRows().addAll(data.getRows());
    return r;
  }
  
  /**
   * Create data set that results from joining
   * several data sets.
   * @param dataSets Data sets to join.
   * @return A new data set containing the rows 
   *   from all given data sets. 
   * @throws InvalidOperationException if <code>dataSets</code>
   * is null or empty or if the data source is not the
   * same for all data sets.
   */
  @SafeVarargs
  public static DataSet join(DataSet... dataSets) throws InvalidOperationException {
    if (dataSets == null || dataSets.length == 0) {
      throw new InvalidOperationException("No source data sets given for joining.");
    }
    DataSet r = copyOf(dataSets[0]);
    for (int i = 1; i < dataSets.length; i++) {
      DataSet d = dataSets[i];
      if (d.getSource() != r.getSource()) {
        throw new InvalidOperationException("Data source mismatch.");
      }
      r.getRows().addAll(d.getRows());
    }
    return r;
  }
  
  @SuppressWarnings("javadoc")
  private void checkIfNotReadOnly() {
    if (readOnly) {
      throw new InvalidOperationException("Data set is read-only.");
    }
  }

  /**
   * Sort rows in data set by row hash code (for testing purposes only).
   */
  final void normalizeRowOrder() {
    Collections.sort(rows, (a,b) -> Integer.compare(a.hashCode(), b.hashCode()));    
  }
  
  /**
   * Test if given data set has the same rows (for testing purposes only).
   * @param other The other data set.
   * @return <code>true</code> if the other data set contains the same
   * rows and in the same order.
   */
  final boolean sameDataAs(DataSet other) {
    return rows.equals(other.rows);
  }
}

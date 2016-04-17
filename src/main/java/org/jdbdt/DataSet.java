package org.jdbdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Data set.
 * 
 * <p>A data set represents a collection of rows.</p>
 * 
 * @since 0.1
 *
 */
public class DataSet {
  
  /**
   * Data source.
   */
  final DataSource source;

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
    this.readOnly = false;
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
  public void setReadOnly() {
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
  public DataSetBuilder build() {
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
  public int size() {
    return rows.size();
  }

  /**
   * Get internal list of rows (package access only)
   * @return Internal ist of rows in the data set.
   */
  final List<Row> getRows() {
    return rows;
  }
  
  /**
   * Clear contents (for package-private use), even if data set
   * is read-only.
   */
  final void clear() {
    rows.clear();
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
  public DataSet add(DataSet other) {
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
   */
  public static DataSet subset(DataSet data, int startIndex, int n) {
    if (startIndex < 0 || n < 0 || startIndex + n >= data.size()) {
      throw new InvalidOperationException("Invalid range.");
    }
    final DataSet sub = new DataSet(data.getSource());
    final int endIndex = startIndex + n;
    final ArrayList<Row> dataRows = data.rows;
    for (int i = startIndex; i < endIndex; i++) {
      sub.rows.add(dataRows.get(i));
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
   */
  public static DataSet singleton(DataSet data, int index) {
    return subset(data, index, 1);
  }
  
  /**
   * Create sub-set with the first <code>n</code> rows.
   * @param data Source data set.
   * @param n Number of rows.
   * @return A new data set containing the first 
   * <code>n</code> rows in the source data set.
   */
  public static DataSet head(DataSet data, int n) {
    return subset(data, 0, n);
  }
  
  /**
   * Create sub-set with the last <code>n</code> rows.
   * @param data Source data set.
   * @param n Number of rows.
   * @return A new data set containing the last 
   *        <code>n</code> rows in the source data set.
   */
  public static DataSet tail(DataSet data, int n) {
    return subset(data, data.size() - n - 1, n);
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

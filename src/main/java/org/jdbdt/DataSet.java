package org.jdbdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
public class DataSet implements Iterable<Row> {
  /**
   * Data source.
   */
  final DataSource source;
  
  /**
   * Rows in the data set.
   */
  private final List<Row> rows;
  
  /**
   * Read-only flag.
   */
  private final boolean readOnly;
  
  /**
   * Constructs a new data set.
   * @param ds Data source.
   */
  DataSet(DataSource ds) {
    this(ds, new ArrayList<>(), false);
  }

  /**
   * Constructs a new data set.
   * @param ds Data source.
   * @param readOnly Read-only flag.
   */
  DataSet(DataSource ds, boolean readOnly) {
    this(ds, new ArrayList<>(), readOnly);
  }
  
  /**
   * Constructs a new row set.
   * @param ds Data source.
   * @param list Row list. 
   * @param readOnly Read-only flag
   */
  DataSet(DataSource ds, List<Row> list, boolean readOnly) {
    this.source = ds;
    this.rows = list;
    this.readOnly = readOnly;
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
   * Check if the data set is read-only.
   * 
   * <p>
   * Adding rows to the data set will result in an exception 
   * of type {@link InvalidOperationException}.
   * </p>
   * 
   * @return <code>true</code> if data set is read-only.
   */
  public final boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Test if set is empty.
   * @return <code>true</code> is  set is empty.
   */
  final boolean isEmpty() {
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
   * Clear contents (package-private).
   */
  final void clear() {
    rows.clear();
  }
  
  /**
   * Add a row to the data set.
   * @param columnValues Column values forming a row. 
   * @return The data set instance (for chained calls).
   */
  public final DataSet row(Object... columnValues) {
    checkIfNotReadOnly();
    if (columnValues.length != source.getColumnCount()) {
      throw new InvalidOperationException(source.getColumnCount() +
            " columns expected, not " + columnValues.length + ".");
    }
    addRow(new RowImpl(columnValues));
    return this;
  }

  
  /**
   * Add rows to the data set.
   * @param rows Array of rows. 
   * @return The data set instance (for chained calls).
   */
  public final DataSet row(Object[][] rows) {
    checkIfNotReadOnly();
    for (Object[] columnValues : rows) {
      addRow(new RowImpl(columnValues));
    }
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
   * Get an iterator for the row set.
   * @return An iterator object.
   */
  @Override
  public Iterator<Row> iterator() {
    return !readOnly ? rows.iterator() 
      : Collections.unmodifiableList(rows).iterator();
  }
  
  @Override
  public boolean equals(Object o) {
    return o == this ||
      ( o instanceof DataSet &&
        rows.equals(((DataSet) o).rows) );
  }
  
  /**
   * Create sub-set of a given data set.
   * @param data Data set.
   * @param startIndex Start index.
   * @param endIndex End index.
   * @return A new data set containing 
   *        the rows in the specified range.
   */
  public static DataSet subset(DataSet data, int startIndex, int endIndex) {
    DataSet sub = new DataSet(data.getSource());
    ListIterator<Row> itr = data.rows.listIterator(startIndex);
    int index = startIndex;
    while (itr.hasNext() && index < endIndex) {
      sub.rows.add(itr.next());
    }
    return sub;
  }
  
  /**
   * Add rows of given data set to this set.
   * @param other This data set.
   * @return The data set instance (for chained calls).
   */
  public DataSet add(DataSet other) {
    checkIfNotReadOnly();
    rows.addAll(other.rows);
    return this;
  }

  @SuppressWarnings("javadoc")
  private void checkIfNotReadOnly() {
    if (readOnly) {
      throw new InvalidOperationException("Data set is read-only.");
    }
  }

  /**
   * Sort data set by row hash code (for testing purposes only).
   */
  void enforceHOrdering() {
    Collections.sort(rows, (a,b) -> Integer.compare(a.hashCode(), b.hashCode()));    
  }
}

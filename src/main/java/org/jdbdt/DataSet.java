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
   * Create a builder for this data set.
   * @return A new builder for this data set.
   */
  public DataSetBuilder build() {
    return new DataSetBuilder(this);
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
   * @throws InvalidOperationException if this data set is read-only.
   * @see #row(Object[][])
   * @see #add(DataSet)
   * @see #isReadOnly()
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
   * @throws InvalidOperationException if this data set is read-only.
   * @see #row(Object...)
   * @see #add(DataSet)
   * @see #isReadOnly()
   */
  public final DataSet row(Object[][] rows) {
    checkIfNotReadOnly();
    for (Object[] columnValues : rows) {
      addRow(new RowImpl(columnValues));
    }
    return this;
  }
  
  /**
   * Add rows of given data set to this data set.
   * 
   * @param other The other data set.
   * @return The data set instance (for chained calls).
   * @throws InvalidOperationException if this data set is read-only.
   * @see #row(Object...)
   * @see #row(Object[][])
   * @see #isReadOnly()
   */
  public DataSet add(DataSet other) {
    checkIfNotReadOnly();
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
   * Get an iterator for the row set.
   * @return An iterator object.
   */
  @Override
  public Iterator<Row> iterator() {
    return !readOnly ? rows.iterator() 
        : Collections.unmodifiableList(rows).iterator();
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

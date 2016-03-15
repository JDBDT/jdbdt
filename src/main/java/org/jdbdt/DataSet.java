package org.jdbdt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Representation of a row set.
 * 
 * <p>
 * This class is used internally by JDBDT to represent database row sets.
 * </p>
 * 
 * @since 0.1
 *
 */
public class DataSet implements Iterable<Row> {
  
  /**
   * Data source.
   */
  final DataSource ds;
  /**
   * Rows in the data set.
   */
  private final ArrayList<Row> rows;
  
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
   * @param l Row list. 
   */
  DataSet(DataSource ds, ArrayList<Row> l) {
    this.ds = ds;
    this.rows = l;
  }
  

  /**
   * Test if set is empty.
   * @return <code>true</code> is  set is empty.
   */
  final boolean isEmpty() {
    return rows.isEmpty();
  }
  
  /**
   * Get size set.
   * @return The number of rows in the set.
   */
  public int size() {
    return rows.size();
  }
  
  /**
   * Clear contents.
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
    if (columnValues.length != ds.getColumnCount()) {
      throw new InvalidUsageException(ds.getColumnCount() +
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
    for (Object[] columnValues : rows) {
      row(columnValues);
    }
    return this;
  }
  
  /**
   * Add a row to the set (package-private version).
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
    return rows.iterator();
  }
}

package org.jdbdt;

import java.sql.SQLException;

/**
 * Database loader.
 * 
 * <p>
 * A database loader is used for insertions
 * into a database table. A loader is created through a call to 
 * {@link JDBDT#insertInto(Table)}.
 * After creation, the {@link #row(Object...)} and {@link #rows(DataSet)}methods
 * may be used to insert data, possibly in a chained sequence of calls.
 * The object types for row data  must be compliant
 * with the JDBC constraints for mapping Java to SQL types
 * (<a href="https://jcp.org/en/jsr/detail?id=221">JDBC 4.x Specification</a>, 
 * Appendix B / "Data Type Conversion Tables").
 * </p> 
 * 
 * 
 * <p><b>Illustration of use</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Loader;
 * import org.jdbdt.Table;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Table t = table("TableName")
 *          .columns("id", "login", "password", "since")
 *          .boundTo(conn);
 * ...
 * insertInto(t)
 *   .row(1, "harry", "pass", Date.valueOf("2015-01-01"))
 *   .row(2, "mark",  "PaSs", Date.valueOf("2015-01-02"))
 *   .row(3, "john",  "paSS", Date.valueOf("2015-01-03"));
 * </pre></blockquote>
 * 
 * 
 * @see Table
 * @see JDBDT#insertInto(Table)
 * @see TypedLoader
 * 
 * @since 0.1
 *
 */
public class Loader {

  /**
   * Table to use.
   */
  private final Table table;
  

  /**
   * Constructor.
   * @param table Table to use.
   */
  Loader(Table table) {
    this.table = table;
  }

  /**
   * Get table associated to this loader.
   * @return The table instance.
   */
  final Table getTable() {
    return table;
  }
  /**
   * Insert a row.
   * @param  row Data for a row value.
   * @return The loader instance (for chained cals).
   * @throws SQLException if a database error occurs during insertion.
   */
  public Loader row(Object... row) throws SQLException {
    table.insertRow(row);
    return this;
  }
  
  /**
   * Insert all the rows defined by a data set. 
   * @param ds Data set.
   * @return The loader instance (for chained calls).
   * @throws SQLException if a database error occurs during insertion.
   */
  public Loader rows(DataSet ds) throws SQLException {
    for (Row r : ds) {
      table.insertRow(r.getColumnData());
    }
    return this;
  }
  
}

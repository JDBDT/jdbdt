package org.jdbdt;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Typed variant of a database loader.
 *
 * <p>
 * An object of this kind extends {@link Loader}
 * and associates to a {@link TypedTable} instance. 
 * A typed loader is created through a call to 
 * {@link JDBDT#insertInto(TypedTable)}.
 * After creation, methods like {@link #row(Object) row(T)} and {@link #rows(Object...) rows(T...)}
 * {@link #rows(Collection)} 
 * may be used to insert data, possibly in a chained sequence of calls.
 * </p> 
 * 
 * <p><b>Illustration of use</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Conversion;
 * import org.jdbdt.TypedLoader;
 * import org.jdbdt.TypedTable;
 * import java.sql.Connection;
 * import java.util.List;
 * ...
 * Connection conn = ...;
 * Conversion&lt;MyClass&gt; conv = ...;
 * TypedTable&lt;MyClass&gt; t = table("TableName",conv)
 *                        .columns("A", "B", "C", "D")
 *                        .boundTo(conn);
 * ...
 * MyClass obj1 = ..., obj2 = ..., obj3 = ...;
 * insertInto(t)
 *   .row(obj1)
 *   .row(obj2)
 *   .row(obj3);
 * ...
 * insertInto(t)
 *   .rows(obj1, obj2, obj3);
 * ...
 * List&lt;MyClass&gt; list1 = ..., list2 = ...;
 * insertInto(t)
 *   .rows(list1)
 *   .rows(list2);
 * </pre></blockquote>
 * 
 * @param <T> Type of objects mapped onto the database. 
 *
 * @see JDBDT#insertInto(TypedTable)
 * @see TypedTable
 * @see Loader
 * @since 0.1
 */
public final class TypedLoader<T> extends Loader {

  /**
   * Constructs a typed loader.
   * @param table Table to use.
   */
  TypedLoader(TypedTable<T> table) {
    super(table);
  }

  /**
   * Perform insertion.
   * @param rowObj Row object.
   * @throws SQLException if a database error occurs.
   */
  private void insert(T rowObj) throws SQLException {
    @SuppressWarnings("unchecked")
    TypedTable<T> t = (TypedTable<T>) super.getTable();
    t.insertRow(t.conversion().convert(rowObj));
  }

  /**
   * Insert single row.
   * @param rowObj Row object.
   * @return The loader instance (for possible chaining).
   * @throws SQLException If a database error occurs during insertion.
   */
  public TypedLoader<T> row(T rowObj) throws SQLException {
    insert(rowObj);
    return this;
  }

  /**
   * Insert several rows at once.
   * @param rowObjects Objects to map onto row values.
   * @return The loader instance (for possible chaining).
   * @throws SQLException If a database error occurs during insertion.
   */
  @SafeVarargs
  public final TypedLoader<T> rows(T... rowObjects) 
      throws SQLException {
    for (T rowObj : rowObjects) {
      insert(rowObj);
    }
    return this;
  }

  /**
   * Insert all rows in a collection.
   * @param rows Collection of row objects.
   * @return The loader instance (for possible chaining).
   * @throws SQLException If a database error occurs during insertion.
   */
  public final TypedLoader<T> rows(Collection<? extends T> rows) 
      throws SQLException {
    for (T rowObj : rows) {
      insert(rowObj);
    }
    return this;
  }
  
  @Override
  public TypedLoader<T> row(Object... row) throws SQLException {
    super.row(row);
    return this;
  }
  
  @Override
  public TypedLoader<T> rows(DataSet ds) throws SQLException {
    super.rows(ds);
    return this;
  }
}

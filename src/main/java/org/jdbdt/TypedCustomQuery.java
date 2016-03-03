package org.jdbdt;

import java.sql.PreparedStatement;

/**
 * Typed query created from a previously prepared SQL statement.
 * 
 * <p>
 * Instances of this class are created using 
 * {@link JDBDT#selectUsing(PreparedStatement, Conversion)}.
 * </p>
 * 
 * <p><b>Example - creation of typed query from custom SQL statements</b>
 * <pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.TypedCustomQuery;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * String customSql = ...;
 * Conversion&lt;MyClass&gt; conv = ...;
 * TypedQuery&lt;MyClass&gt; q = selectUsing(conn.prepareStament(customSql), conv);
 * </pre>
 * </p>
 * 
 * @param <T> Type of objects.
 * 
 * @see Conversion
 * @see Query
 * @see JDBDT#selectUsing(PreparedStatement, Conversion)
 * 
 * @since 0.1
 */
final class TypedCustomQuery<T> extends Query implements TypedQuery<T> {

  /**
   * Conversion function.
   */
  private final Conversion<T> conv;
  
  /**
   * Constructs a typed query.
   * @param stmt Statement.
   * @param conv Conversion function.
   */
   TypedCustomQuery(PreparedStatement stmt, Conversion<T> conv) {
    super(stmt);
    this.conv = conv;
  }

  /**
   * Get conversion function.
   * @return The conversion function in used.
   */
  @Override
  public Conversion<T> conversion() {
    return conv;
  }

}

package org.jdbdt;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Typed variant for database table representation.
 * 
 * <p>
 * Objects of this kind are created through a call to {@link JDBDT#table(String,Conversion)},
 * and allow for a simple form of (unidirectional) object-relational mapping (ORM) through a
 * conversion from object to row format (a {@link Conversion} instance).
 * </p>
 * 
 * <p><b>Illustration 1</b></p>
 * <pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.TypedTable;
 * import org.jdbdt.Conversion;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * Conversion&lt;MyClass&gt; conv = ...;
 * TypedTable&lt;MyClass&gt; t = table("TableName", conv)
 *                        .columns("A", "B", "C", "D")
 *                        .boundTo(conn);
 * </pre>
 * 
 * <p><b>Illustration 2</b></p>
 * <p>The following makes use of lambda expressions to
 * define the {@link Conversion} instance:</p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.TypedTable;
 * import java.sql.Connection;
 * ...
 * Connection conn = ...;
 * TypedTable&lt;MyClass&gt; t = 
 *   table("TableName",
 *         myObj -&gt; new Object[] {
 *                    myObj.getA(), 
 *                    myObj.getB(), 
 *                    myObj.getC(),
 *                    myObj.getD() })
 *   .columns("A", "B", "C", "D")
 *   .boundTo(conn);
 * </pre></blockquote>
 * 
 * 
 * @param <T> Type of objects at stake.
 * @see JDBDT#table(String, Conversion)
 * @see Conversion
 * @see Table
 * @since 0.1
 */
public final class TypedTable<T> extends Table {

  /** Conversion function. */
  private final Conversion<T> conv;
  
  /**
   * Constructs a typed table.
   * 
   * @param name Table name.
   * @param conv Conversion function.
   * @see JDBDT#table(String)
   */
  TypedTable(String name, Conversion<T> conv) {
    super(name);
    this.conv = conv;
  }
  
  /**
   * Get conversion function.
   * @return The conversion function associated to this typed table.
   */
  Conversion<T> conversion() {
    return conv;
  }
  
  @Override
  public final TypedTable<T> columns(String... columnNames) {
    super.columns(columnNames);
    return this;
  }
  
  @Override
  public final TypedTable<T> boundTo(Connection c) throws SQLException {
    super.boundTo(c);
    return this;
  }

}

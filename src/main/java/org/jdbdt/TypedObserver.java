package org.jdbdt;

/**
 * Typed variant of a database observer.
 * 
 * @param <T> Type of objects for the observer.
 */
public final class TypedObserver<T> extends Observer {

  /**
   * Conversion function.
   */
  private final Conversion<T> conv;

  /**
   * Constructs a typed observer for a given table.
   * If the given row set is non-null it will be assumed 
   * as the initial observation state. Otherwise,
   * a table query will be executed upon construction.
   * 
   * @param t Table.
   * @param initial Initial row set to assume (ignored if null).
   */
  TypedObserver(TypedTable<T> t, RowSet initial) {
    super(t, initial);
    conv = t.conversion();
  }
  
  /**
   * Get conversion function.
   * @return Conversion function associated to the typed observer.
   */
  Conversion<T> conversion() {
    return conv;
  }
  
  /**
   * Constructs a typed observer for a given query.
   * If the given row set is non-null it will be assumed 
   * as the initial observation state. Otherwise,
   * the query will be executed upon construction.
   * 
   * @param query Database query.
   * @param queryArgs Query arguments.
   * @param initial Initial row set to assume (ignored if null).
   */
  TypedObserver(TypedTableQuery<T> query, Object[] queryArgs, RowSet initial) {
    super(query, queryArgs, initial);
    this.conv = query.conversion();
  }
  
  @Override
  final TypedDelta<T> getDelta() {
    RowSet s1 = getLastObservation();
    super.refresh();
    RowSet s2 = getLastObservation();
    return new TypedDelta<>(this, s1, s2);
  }

}

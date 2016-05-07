package org.jdbdt;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBDT main API facade.
 * 
 * <p>
 * This utility class defines the front-end API database 
 * verification, setup, tear-down, as well as JDBDT
 * object creation.
 * </p>
 *  
 * @since 0.1
 */
public final class JDBDT {

  /**
   * Private constructor to avoid unintended instantiation.
   */
  private JDBDT() { }

  /**
   * Version identifier.
   */
  private static final String VERSION = "0.1.0-SNAPSHOT";

  /**
   * Get JDBDT version identifier.
   * @return An identifier for the JDBDT version.
   */
  public static String version() { 
    return VERSION;
  }
  
  /**
   * Create a new database handle.
   * 
   * <p>The given connection is associated to the handle.
   * A reference to the connection can later be retrieved 
   * using {@link DB#getConnection()}.</p>
   * 
   * @param c Connection.
   * @return A new database handle for the given connection.
   */
  public static DB database(Connection c) {
    return new DB(c);
  }
  
  /**
   * Create a builder for a fresh data set.
   * @param source Data source.
   * @return A new data set builder.
   */
  public static DataSetBuilder builder(DataSource source) {
    return new DataSet(source).build();
  }

  /**
   * Get empty data set constant for given data source.
   * 
   * <p>
   * This convenience method is useful to denote the empty data set for 
   * a data source. It always returns an empty, read-only data set,
   * and one that is guaranteed to be unique and for the given 
   * data source instance.
   * </p>
   * 
   * @param source Data source.
   * @return A new data set for the given source.
   * @see DataSet#isEmpty()
   * @see DataSet#isReadOnly()
   */
  public static DataSet empty(DataSource source) {
    return source.theEmptySet();
  }
  
  /**
   * Create a new data set.
   * @param source Data source.
   * @return A new data set for the given source.
   */
  public static DataSet data(DataSource source) {
    return new DataSet(source);
  }

  /**
   * Create a new typed data set.
   * @param <T> Type of objects.
   * @param source Data source.
   * @param conv Conversion function.
   * @return A new typed data set for the given source.
   */
  public static <T> TypedDataSet<T> data(DataSource source, Conversion<T> conv) {
    return new TypedDataSet<>(source, conv);
  }

  /**
   * Create a table data source.
   * @param db Database handle.
   * @param name Table name.
   * @return A new {@link Table} instance.
   */
  public static Table table(DB db, String name) {
    return new Table(db, name);
  }
  
  /**
   * Create a query data source from given SQL code.
   * @param db Database handle.
   * @param sql SQL code.
   * @param args Optional query arguments.
   * @return A new query data source.
   */
  public static Query query(DB db, String sql, Object... args) {
    return new Query(db, sql, args);
  }
  
  /**
   * Create a query builder.
   * @param db Database handle.
   * @param columns Columns for query.
   * @return A new query builder.
   */
  public static QueryBuilder select(DB db, String... columns) {
    return new QueryBuilder(db, columns);
  }
  

  /**
   * Take a database snapshot.
   * 
   * <p>
   * The method takes a snapshot of the current database state
   * for the given data source. A fresh query will be issued, 
   * yielding a new data set that is recorded internally 
   * as the latest snapshot for the data source. 
   * The snapshot is used as the base reference for any subsequent 
   * delta assertions, until a new snapshot is defined with
   * a new call to this method or to {@link #populate(DataSet)};
   * </p>
   * 
   * <p>
   * Note that the method returns the data set instance representing the snapshot,
   * but assertion methods do not require any book-keeping 
   * by the caller, hence the result may be ignored for this
   * purpose. Note also that the returned
   * data set is read-only (see {@link DataSet#isReadOnly()}).
   * </p>
   * 
   * @param source Data source.
   * @throws UnexpectedDatabaseException if a database error occurs 
   * @return Data set representing the snapshot.
   * 
   * @see #assertDelta(DataSet, DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static DataSet
  takeSnapshot(DataSource source)  {
    return source.executeQuery(CallInfo.create(), true);
  }

  /**
   * Query the data source, without setting a snapshot.
   * @param source Data source.
   * @return Query result.
   */
  static DataSet
  executeQuery(DataSource source)  {
    return source.executeQuery(CallInfo.create(), false);
  }
  
  /**
   * Assert that no changes occurred for the given data source 
   * (error message variant).
   * 
   * @param message The message for assertion error.
   * @param source Data source. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(String, DataSet)
   * @see #assertInserted(String, DataSet)
   */
  public static void assertUnchanged(String message, DataSource source) throws DBAssertionError {
    DataSet emptyDataSet = empty(source);
    DBAssert.deltaAssertion(CallInfo.create(message), emptyDataSet, emptyDataSet);
  }
  /**
   * Assert that no changes occurred for the given data source.
   * 
   * @param source Data source. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertUnchanged(DataSource source) throws DBAssertionError {
    DataSet emptyDataSet = empty(source);
    DBAssert.deltaAssertion(CallInfo.create(), emptyDataSet, emptyDataSet);
  }

  /**
   * Assert that database changed only by removal of a given
   * data set (error message variant).
   * 
   * @param message The message for the assertion error.
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(String, DataSet,DataSet)
   * @see #assertInserted(String, DataSet)
   * @see #assertUnchanged(String, DataSource)
   */
  public static void assertDeleted(String message, DataSet data) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(message), data, empty(data.getSource())); 
  }
  
  /**
   * Assert that database changed only by removal of a given
   * data set. 
   * 
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertInserted(DataSet)
   */
  public static void assertDeleted(DataSet data) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(), data, empty(data.getSource())); 
  }

  /**
   * Assert that database changed only by addition of a given
   * data set (error message variant).
   * 
   * @param message The message for assertion error.
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(String message, DataSet data) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(message), empty(data.getSource()), data);
  }
  
  /**
   * Assert that database changed only by addition of a given
   * data set.
   *
   * @param data data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(DataSet data) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(), empty(data.getSource()), data);
  }

  /**
   * Assert database delta expressed by 'old' 
   * and 'new' data sets (error message variant).
   * 
   * @param message The error message for the assertion error.
   * @param oldData Expected 'old' data.
   * @param newData Expected 'new' data.
   * @throws DBAssertionError if the assertion fails.
   *
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertDelta(String message, DataSet oldData, DataSet newData) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(message), oldData, newData);
  }
  
  /**
   * Assert database delta expressed by 'old' 
   * and 'new' data sets.
   * 
   * @param oldData Expected 'old' data.
   * @param newData Expected 'new' data.
   * @throws DBAssertionError if the assertion fails.
   *
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertDelta(DataSet oldData, DataSet newData) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(), oldData, newData);
  }
  
  /**
   * Assert that the database state matches the given data set 
   * (error message variant).
   * @param message The message for assertion error.
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   */
  public static void assertState(String message, DataSet data) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(message), data);
  }
  
  /**
   * Assert that the database state matches the given data set.
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   */
  public static void assertState(DataSet data) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(), data);
  }
  
  /**
   * Insert a data set onto database.
   * 
   * @param data Data set for insertion.
   * @throws SQLException If a database error occurs.
   *  
   */
  public static void insert(DataSet data) throws SQLException {
    DBSetup.insert(CallInfo.create(), data);
  }

  /**
   * Populate database with given data set.
   * 
   * @param data Data set for insertion.
   * @throws SQLException If a database error occurs.
   *  
   */
  public static void populate(DataSet data) throws SQLException {
    DBSetup.populate(CallInfo.create(), data);
  }

  /**
   * Delete all data from a table. 
   *
   * @param table Table.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @see #truncate(Table)
   * @see #deleteAllWhere(Table,String,Object...)
   */
  public static int deleteAll(Table table) throws SQLException  {
    return DBSetup.deleteAll(CallInfo.create(), table);
  }


  /**
   * Delete all data from a table, subject to a <code>WHERE</code>
   * clause. 
   * 
   *
   * @param table Table.
   * @param where <code>WHERE</code> clause
   * @param args <code>WHERE</code> clause arguments, if any.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @see #deleteAll(Table)
   * @see #truncate(Table)
   */
  public static int deleteAllWhere(Table table, String where, Object... args) 
  throws SQLException  {
    return DBSetup.deleteAll(CallInfo.create(), table, where, args);
  }

  /**
   * Truncate table.
   * @param t Table.
   * @throws SQLException if a database error occurs.
   * @see #deleteAll(Table)
   * @see #deleteAllWhere(Table,String,Object...)
   */
  public static void truncate(Table t) throws SQLException  {
    DBSetup.truncate(CallInfo.create(), t);
  }

}

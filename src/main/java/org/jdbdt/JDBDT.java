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
   * Create a database handle.
   * 
   * <p>The given connection is associated to the handle.
   * A reference to the connection can later be retrieved 
   * using {@link DB#getConnection()}</p>.
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
  query(DataSource source)  {
    return source.executeQuery(CallInfo.create(), false);
  }
  
  /**
   * Assert that no changes were made to the database.
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>delta(sp).end()</code>.
   * </p>
   * 
   * @param source Data source. 
   * @throws DBAssertionError 
   *         if there are unverified changes for the delta
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertUnchanged(DataSource source) throws DBAssertionError {
    DataSet emptyDataSet = empty(source);
    assertDelta(CallInfo.create(), emptyDataSet, emptyDataSet);
  }

  /**
   * Assert that database changed only by removal of a given
   * data set.
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>delta(source).before(data).end()</code>.
   * </p>
   * 
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertInserted(DataSet)
   */
  public static void assertDeleted(DataSet data) throws DBAssertionError {
    assertDelta(CallInfo.create(), data, empty(data.getSource())); 
  }

  /**
   * Assert that database changed only by addition of a given
   * data set.
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>delta(s).after(data).end()</code>.
   * </p>
   * 
   * @param data data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(DataSet data) throws DBAssertionError {
    assertDelta(CallInfo.create(), empty(data.getSource()), data);
  }

  /**
   * Assert that database changed according
   * to given 'before' and 'after' data sets. 
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>delta(s).before(b).after(a).end()</code>.
   * </p>
   * 
   * @param pre Old data no longer in database.
   * @param post New Data in database.
   * @throws DBAssertionError if the assertion fails.
   *
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertDelta(DataSet pre, DataSet post) throws DBAssertionError {
    assertDelta(CallInfo.create(), pre, post);
  }
  
  /**
   * The primitive delta assertion method.
   * @param callInfo Call Info.
   * @param oldData Expected old data.
   * @param newData Expected new data.
   * @throws DBAssertionError if the assertion fails.
   */
  private static void assertDelta(CallInfo callInfo, DataSet oldData, DataSet newData) throws DBAssertionError {
    if (oldData.getSource() != newData.getSource()) {
      throw new InvalidOperationException("Data sets associate to different data sources.");
    }
    new Delta(callInfo, oldData.getSource())
       .before(oldData)
       .after(newData)
       .end(); 
  }

  /**
   * Assert database state is the given data set.
   * @param data Data set.
   * @throws DBAssertionError if the assertion fails.
   */
  public static void assertState(DataSet data) throws DBAssertionError {
    new Delta(CallInfo.create(), data).end(); 
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

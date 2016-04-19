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
   * @see #delta(DataSource)
   */
  public static DataSet
  takeSnapshot(DataSource source)  {
    return source.executeQuery(true);
  }

  
  /**
   * Obtain delta.
   * 
   * <p>
   * A new query will be issued for the given 
   * data source, and the resulting delta will reflect 
   * the difference between the last snapshot 
   * and the current database state.
   * </p>
   * 
   * @param s Data source.
   * @return A new {@link Delta} object.
   * @see #takeSnapshot(DataSource)
   */
  static Delta delta(DataSource s) {
    return new Delta(s);
  }


  /**
   * Assert that no changes were made to the database.
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>delta(sp).end()</code>.
   * </p>
   * 
   * @param sp SnapshotProvider provider.
   * @throws DatabaseAssertionError 
   *         if there are unverified changes for the delta
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertUnchanged(DataSource sp) throws DatabaseAssertionError {
    delta(sp).end(); 
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
   * @throws DatabaseAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertInserted(DataSet)
   */
  public static void assertDeleted(DataSet data) throws DatabaseAssertionError {
    delta(data.getSource()).before(data).end(); 
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
   * @throws DatabaseAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(DataSet data) throws DatabaseAssertionError {
    delta(data.getSource()).after(data).end(); 
  }

//  /**
//   * Assert that database was updated according
//   * to given data set. 
//   * 
//   * <p>
//   * This method behaves like {@link #assertUpdated(DataSet, DataSet)}
//   * but does not verify which (old) rows were updated, 
//   * but merely that there were exactly as much rows 
//   * updated as those given in the data set argument.
//   * </p>
//   * 
//   * @param data New Data in database.
//   * @throws DatabaseAssertionError if the assertion fails.
//   *
//   * @see #assertNoChanges(DataSource)
//   * @see #assertDeleted(DataSet)
//   * @see #assertInserted(DataSet)
//   */
//  public static void assertUpdated(DataSet data) throws DatabaseAssertionError {
//    Delta d = delta(data.getSource());
//    d.after(data);
//    if (d.size() != data.size()) {
//      throw new DatabaseAssertionError("Expected equal 
//    }
//  }

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
   * @throws DatabaseAssertionError if the assertion fails.
   *
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertDelta(DataSet pre, DataSet post) throws DatabaseAssertionError {
    if (pre.getSource() != post.getSource()) {
      throw new InvalidOperationException("Data sets associate to different data sources.");
    }
    delta(pre.getSource()).before(pre).after(post).end(); 
  }

  /**
   * Assert database state is the given data set.
   * @param data Data set.
   * @throws DatabaseAssertionError if the assertion fails.
   */
  public static void assertState(DataSet data) throws DatabaseAssertionError {
    new Delta(data).end(); 
  }
  /**
   * Insert a data set onto database.
   * 
   * @param data Data set for insertion.
   * @throws SQLException If a database error occurs.
   *  
   */
  public static void insert(DataSet data) throws SQLException {
    DBSetup.insert(data);
  }

  /**
   * Populate database with given data set.
   * 
   * @param data Data set for insertion.
   * @throws SQLException If a database error occurs.
   *  
   */
  public static void populate(DataSet data) throws SQLException {
    DBSetup.populate(data);
  }

  /**
   * Delete all data from a table. 
   *
   * @param table Table.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @see #truncate(Table)
   * @see #deleteAll(Table,String,Object...)
   */
  public static int deleteAll(Table table) throws SQLException  {
    return DBSetup.deleteAll(table);
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
  public static int deleteAll(Table table, String where, Object... args) 
  throws SQLException  {
    return DBSetup.deleteAll(table, where, args);
  }

  /**
   * Truncate table.
   * @param t Table.
   * @throws SQLException if a database error occurs.
   * @see #deleteAll(Table)
   * @see #deleteAll(Table,String,Object...)
   */
  public static void truncate(Table t) throws SQLException  {
    DBSetup.truncate(t);
  }

}

package org.jdbdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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
   * Create a database handle for given SQL connection.
   * @param c Connection.
   * @return A new database handle for the given connection.
   */
  public static DB db(Connection c) {
    return new DB(c);
  }
  
  /**
   * Create a new data set builder.
   * @param source Data source.
   * @return A new data set builder for the given source.
   */
  public static DataSetBuilder build(DataSource source) {
    return new DataSetBuilder(source);
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
   * a new call to this method or to {@link #assumeSnapshot(DataSet)}.
   * </p>
   * 
   * <p>
   * Note that the method returns the data set instance representing the snapshot,
   * but assertion methods do not require any book-keeping 
   * by the caller, hence the result may be ignored for this
   * purpose. Note also that this data set is read-only (see {@link DataSet#isReadOnly()}).
   * </p>
   * @param source Data source.
   * @throws UnexpectedDatabaseException if a database error occurs 
   * @return Data set representing the snapshot.
   * 
   * @see #assumeSnapshot(DataSet)
   * @see #assertChanged(DataSet, DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see #delta(DataSource)
   */
  public static DataSet
  snapshot(DataSource source)  {
    return source.executeQuery(true);
  }

  /**
   * Assume the given data set as a snapshot for current database state.-
   * @param data Data set.
   */
  public static void
  assumeSnapshot(DataSet data)  {
    data.getSource().setSnapshot(data);
  }
  
  /**
   * Obtain delta.
   * 
   * <p>
   * A new query will be issued for the given 
   * snapshot provider instance, and the resulting delta will reflect 
   * the difference between the last snapshot 
   * (see {@link #snapshot(DataSource)}) and the current database state.
   * </p>
   * 
   * @param s Data source.
   * @return A new {@link Delta} object.
   * @see #snapshot(DataSource)
   */
  public static Delta delta(DataSource s) {
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
   * @see #assertChanged(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
   */
  public static void assertNoChanges(DataSource sp) throws DatabaseAssertionError {
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
   * @see #assertNoChanges(DataSource)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
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
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertChanged(DataSet,DataSet)
   * @see Delta#end()
   */
  public static void assertInserted(DataSet data) throws DatabaseAssertionError {
    delta(data.getSource()).after(data).end(); 
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
   * @throws DatabaseAssertionError if the assertion fails.
   *
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
   */
  public static void assertChanged(DataSet pre, DataSet post) throws DatabaseAssertionError {
    if (pre.getSource() != post.getSource()) {
      throw new InvalidUsageException("Data sets associate to different data sources.");
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
   * @param t Table.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @see #truncate(Table)
   * @see #deleteAll(Query)
   */
  public static int deleteAll(Table t) throws SQLException  {
    return DBSetup.deleteAll(t);
  }


  /**
   * Delete all data returned by a table query. 
   * 
   * <p>
   * The query's where clause will 
   * be used for the DELETE statement that is issued.
   * 
   * This method cannot be used with 
   * queries that have GROUP BY or HAVING clauses set.
   * </p>
   *
   * @param q query.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @throws InvalidUsageException if the query has 
   *    a GROUP BY or HAVING clause set, or if no WHERE
   *    clause is set for it.
   * @see #deleteAll(Table)
   * @see #truncate(Table)
   */
  public static int deleteAll(Query q) throws SQLException  {
    return DBSetup.deleteAll(q);
  }

  /**
   * Truncate table.
   * @param t Table.
   * @throws SQLException if a database error occurs.
   * @see #deleteAll(Table)
   * @see #deleteAll(Query)
   */
  public static void truncate(Table t) throws SQLException  {
    DBSetup.truncate(t);
  }
  
  /**
   * Create a file log.
   * 
   * <p>
   * A log can be used for reporting or debugging purposes,
   * and has an XML format.
   * Various types of JDBDT objects may be written to a log
   * e.g. {@link DataSet} or {@link Delta}.  
   * Note that output data is only written when {@link Log#close()}
   * is called on the returned log instance.
   * </p>
   * @param file Output file.
   * @return A log instance.
   * @throws FileNotFoundException If the output file cannot be created.
   * 
   * @see #log(PrintStream)
   */
  public static Log log(File file) throws FileNotFoundException {
    return new Log(file);
  }

  /**
   * Create a log that is written to a stream.
   * 
   * <p>
   * This method has a similar functionality to {@link #log(File)},
   * but redirects log output to the given stream. 
   * </p>
   * 
   * @param out Output stream.
   * @return A log instance.
   * 
   * @see #log(File)
   */
  public static Log log(PrintStream out) {
    return new Log(out);
  }

}

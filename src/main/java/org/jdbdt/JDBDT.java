package org.jdbdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * JDBDT API facade.
 * 
 * <p>The facade defines the standard API
 * for use by test code for tasks like database 
 * verification, setup, and tear-down.
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
   * Get JDBDT version.
   * @return The version identifier for the JDBDT library in use.
   */
  public static String version() { 
    return VERSION;
  }

  /**
   * Create handle for given table name.
   * @param tableName Name of the database table.
   * @return A new {@link Table} instance.
   */
  public static Table table(String tableName) {
    return new Table(tableName); 
  }


  /**
   * Create data builder for a table.
   * @param t Table.
   * @return A new {@link DataBuilder} object.
   */
  public static DataBuilder build(Table t) {
    return new DataBuilder(t);
  }

  /**
   * Create a new data set.
   * @param source Data source instance.
   * @return A new, empty data set.
   */
  public static DataSet data(DataSource source) {
    return new DataSet(source);
  }

  /**
   * Create typed data set.
   * @param <T> Type of objects.
   * @param source Data source.
   * @param conv Conversion function.
   * @return A new, empty data set.
   */
  public static <T> TypedDataSet<T> data(DataSource source, Conversion<T> conv) {
    return new TypedDataSet<>(source, conv);
  }

  /**
   * Create query for a table.
   * @param t Table.
   * @return A new query object.
   */
  public static Query selectFrom(Table t) {
    return new Query(t); 
  }

  /**
   * Take a database snapshot.
   * 
   * <p>
   * The method takes a snapshot of the current database state
   * for the given snapshot provider (e.g., a {@link Table} 
   * or a {@link Query} instance).
   * </p>
   * @param sp SnapshotProvider provider.
   * @throws UnexpectedDatabaseException if a database error occurs 
   *   
   */
  public static void
  snapshot(DataSource sp)  {
    sp.executeQuery(true);
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
   * @throws DeltaAssertionError 
   *         if there are unverified changes for the delta
   * @see #assertChanged(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
   */
  public static void assertNoChanges(DataSource sp) throws DeltaAssertionError {
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
   * @throws DeltaAssertionError if the assertion fails.
   * @see #assertNoChanges(DataSource)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
   */
  public static void assertDeleted(DataSet data) throws DeltaAssertionError {
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
   * @throws DeltaAssertionError if the assertion fails.
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertChanged(DataSet,DataSet)
   * @see Delta#end()
   */
  public static void assertInserted(DataSet data) throws DeltaAssertionError {
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
   * @throws DeltaAssertionError if the assertion fails.
   *
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see Delta#end()
   */
  public static void assertChanged(DataSet pre, DataSet post) throws DeltaAssertionError {
    if (pre.getSource() != post.getSource()) {
      throw new InvalidUsageException("Data sets associate to different data sources.");
    }
    delta(pre.getSource()).before(pre).after(post).end(); 
  }

  
  /**
   * Assert database state is the given data set.
   * @param data Data set.
   * @throws DeltaAssertionError if the assertion fails.
   */
  public static void assertState(DataSet data) throws DeltaAssertionError {
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
   * Execute an arbitrary SQL statement.
   * 
   * <p>
   * Calling this method differs from
   * <code>c.prepareStatement(sql).execute()</code>
   * as follows: an internal, JDBC-driver independent, pool
   * of statements ensures that the SQL code is compiled
   * only once for the given connection, i.e., the 
   * associated {@link PreparedStatement} is generated
   * only once. For SQL code that executes repeatedly,
   * this may be more efficient. This also ensures
   * that SQL code executed in this manner
   * minimizes the use of the actual JDBC driver statement
   * pool (if any), and possible impact on the
   * the SUT components' performance.
   * </p>
   * 
   * @param c Database connection.
   * @param sqlCode SQL code.
   * @throws SQLException If a database error occurs.
   */
  public static void sql(Connection c, String sqlCode) throws SQLException {
    StatementPool.compile(c, sqlCode).execute();
  }

  /**
   * Disable statement pooling.
   * 
   * <p>
   * JDBDT uses an internal statement pool to re-use
   * compiled SQL statements.
   * The pooling scheme is enabled by default, but
   * for some JDBC drivers (e.g. SQLite) re-using
   * SQL statements may cause problems. 
   * In those cases, pooling should be turned off
   * explicitly using this method.
   * </p> 
   * 
   */
  public static void disableStatementPooling() {
    StatementPool.disable();
  }

  /**
   * Create a log that is written to a file.
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
   * @see #logErrorsTo(Log)
   * @see #logErrorsTo(PrintStream)
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
   * @see #logErrorsTo(Log)
   * @see #logErrorsTo(PrintStream)
   */
  public static Log log(PrintStream out) {
    return new Log(out);
  }

  /**
   * Log delta assertion errors to given log instance.
   * 
   * <p>
   * For a non-null log instance argument, whenever an assertion error is 
   * thrown by the JDBDT API ({@link DeltaAssertionError}),
   * the unverified portion of the delta object at stake ({@link Delta})
   * will be logged. A null argument disables logging of this kind,
   * causing the previous log to be closed.
   * </p>
   * 
   * 
   * @param out Output log.
   *
   * @see #logErrorsTo(PrintStream)
   * @see #log(File)
   * @see #log(PrintStream)
   * @see Log#close()
   */
  public static void logErrorsTo(Log out) {
    if (errorLog != null) {
      errorLog.close();
    }
    errorLog = out;
  }

  /**
   * Log assertion errors to given output stream.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>logErrorsTo(log(out)))</code>
   * @param out Output stream.
   * 
   * @see #logErrorsTo(Log)
   * @see #log(PrintStream)
   * @see #log(File)
   */
  public static void logErrorsTo(PrintStream out) {
    logErrorsTo(log(out));
  }

  /**
   * Error log in use (initially null).
   */
  private static Log errorLog = null;

  // TODO
  //  @SuppressWarnings("javadoc")
  //  private static <S extends SnapshotProvider> S logSetup(S s) {
  //    if (errorLog != null) {
  //      s.logErrorsTo(errorLog);
  //    }
  //    return s;
  //  }
}

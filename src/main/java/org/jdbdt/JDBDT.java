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
   * Create typed handle for given table name
   * and conversion function.
   * @param <T> Type of data.
   * @param tableName Name of the database table.
   * @param conv Conversion function.
   * @return A new {@link TypedTable} instance.
   */
  public static <T> TypedTable<T> table(String tableName, Conversion<T> conv) {
    return new TypedTable<>(tableName, conv); 
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
   * Create data set (untyped version).
   * @param source Data source instance.
   * @return A new, empty data set.
   */
  public static DataSet data(DataSource source) {
    return new DataSet(source);
  }
  
  /**
   * Create data set - typed table version.
   * @param <T> Type of objects.
   * @param tt Typed table.
   * @return A new, empty data set.
   */
  public static <T> TypedDataSet<T> data(TypedTable<T> tt) {
    return new TypedDataSet<>(tt);
  }
  
  /**
   * Create data set - typed query version.
   * @param <T> Type of objects.
   * @param tq Typed query.
   * @return A new, empty data set.
   */
  public static <T> TypedDataSet<T> data(TypedQuery<T> tq) {
    return new TypedDataSet<>(tq);
  }
  
  /**
   * Create query for a table.
   * @param t Table.
   * @return A new {@link Query} object.
   */
  public static Query selectFrom(Table t) {
    return new Query(t); 
  }
  
  /**
   * Create query for a typed table.
   * @param <T> Type of objects.
   * @param t Table.
   * @return A new {@link TypedQuery} object.
   */
  public static <T> TypedQuery<T> selectFrom(TypedTable<T> t) {
    return new TypedQuery<>(t); 
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
   * Obtain typed delta - typed table variant.
   * 
   * @param <T> type of objects.
   * @param tt Typed table.
   * @return A new {@link Delta} object.
   * @see #snapshot(DataSource)
   */
  public static <T> TypedDelta<T> delta(TypedTable<T> tt) {
    return new TypedDelta<>(tt);
  }
  
  /**
   * Obtain typed delta - typed query variant.
   * 
   * @param <T> type of objects.
   * @param tq Typed query.
   * @return A new {@link Delta} object.
   * @see #snapshot(DataSource)
   */
  public static <T> TypedDelta<T> delta(TypedQuery<T> tq) {
    return new TypedDelta<>(tq);
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
   * @see #assertChanged(DataSource,DataSet,DataSet)
   * @see #assertDeleted(DataSource,DataSet)
   * @see #assertInserted(DataSource,DataSet)
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
   * @param source Data source.
   * @param data Data set.
   * @throws DeltaAssertionError if the assertion fails.
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSource,DataSet)
   * @see #assertInserted(DataSource,DataSet)
   * @see Delta#end()
   */
  public static void assertDeleted(DataSource source, DataSet data) throws DeltaAssertionError {
    delta(source).before(data).end(); 
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
   * @param s Data source.
   * @param data data set.
   * @throws DeltaAssertionError if the assertion fails.
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSource,DataSet)
   * @see #assertChanged(DataSource,DataSet,DataSet)
   * @see Delta#end()
   */
  public static void assertInserted(DataSource s, DataSet data) throws DeltaAssertionError {
    delta(s).after(data).end(); 
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
   * @param s Data source.
   * @param a 'after' set.
   * @param b 'before' set.
   * @throws DeltaAssertionError if the assertion fails.
   *
   * @see #assertNoChanges(DataSource)
   * @see #assertDeleted(DataSource,DataSet)
   * @see #assertInserted(DataSource,DataSet)
   * @see Delta#end()
   */
  public static void assertChanged(DataSource s, DataSet a, DataSet b) throws DeltaAssertionError {
    delta(s).before(b).after(a).end(); 
  }
  
  /**
   * Create a database loader to insert data onto a table.
   * 
   * @param table Database table.
   * @return A new {@link Loader} instance. 
   */
  public static Loader insertInto(Table table) {
    return new Loader(table);
  }
  
  /**
   * Create a database loader to insert data onto a typed table.
   * 
   * @param <T> Type of objects.
   * @param table Database table.
   * @return A new {@link TypedLoader} instance. 
   */
  public static <T> TypedLoader<T> insertInto(TypedTable<T> table) {
    return new TypedLoader<>(table);
  }

  /**
   * Delete all data from a table. 
   *
   * @param t Table.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @see #truncate(Table)
   * @see #deleteAll(Query,Object...)
   */
  public static int deleteAll(Table t) throws SQLException  {
    return StatementPool.delete(t).executeUpdate();
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
   * @param args Optional query arguments.
   * @return Number of deleted entries.
   * @throws SQLException if a database error occurs.
   * @throws InvalidUsageException if the query has 
   *    a GROUP BY or HAVING clause set, or if no WHERE
   *    clause is set for it.
   * @see #deleteAll(Table)
   * @see #truncate(Table)
   */
  public static int deleteAll(Query q, Object... args) throws SQLException  {
    if (q.groupByClause() != null) {
      throw new InvalidUsageException("GROUP BY clause is set!");
    }
    if (q.havingClause() != null) {
      throw new InvalidUsageException("HAVING clause is set!");
    }
    String whereClause = q.whereClause();
    
    if (whereClause == null) {
      throw new InvalidUsageException("WHERE clause is not set!");
    }
    PreparedStatement deleteStmt = 
      StatementPool.compile(q.getConnection(), 
          "DELETE FROM " + q.getTable().getName() 
          + " WHERE " + whereClause);
    if (args != null && args.length > 0) {
      for (int i=0; i < args.length; i++) {
        deleteStmt.setObject(i + 1, args[i]);
      }
    }
    return deleteStmt.executeUpdate();
  }
  
  /**
   * Truncate table.
   * @param t Table.
   * @throws SQLException if a database error occurs.
   * @see #deleteAll(Table)
   * @see #deleteAll(Query,Object...)
   */
  public static void truncate(Table t) throws SQLException  {
     StatementPool.truncate(t).execute();
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
    StatementPool.disablePooling();
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

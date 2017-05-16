package org.jdbdt;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBDT facade.
 * 
 * <p>
 * This utility class defines the front-end API for database 
 * assertions, setup operations, and creation of associated
 * class instances.
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
   * Get JDBDT version identifier.
   * @return An identifier for the JDBDT version.
   */
  public static String version() { 
    return VersionInfo.ID;
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
   * Create a new database handle for given database URL.
   * 
  * <p>Calling this method is shorthand for:<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;
   * <code>database( DriverManager.getConnection(url) )</code>.
   * </p>
   * 
   * @param url Database URL.
   * @return A new database handle for the given connection.
   * @throws SQLException If the connection cannot be created.
   * @see #database(Connection)
   * @see DriverManager#getConnection(String)
   */
  public static DB database(String url) throws SQLException {
    return database(DriverManager.getConnection(url));
  }
  
  /**
   * Create a new database handle for given database
   * URL, user and password .
   * 
   * <p>Calling this method is shorthand for:<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;
   * <code>database( DriverManager.getConnection(url, user, pass) )</code>.
   * </p>
   * 
   * @param url Database URL.
   * @param user Database user.
   * @param pass Database password.
   * @return A new database handle for the given connection.
   * @throws SQLException If the connection cannot be created.
   * @see #database(Connection)
   * @see DriverManager#getConnection(String, String, String)
   */
  public static DB database(String url, String user, String pass) throws SQLException {
    return database(DriverManager.getConnection(url, user, pass));
  }
  
  /**
   * Tear-down a database handle.
   * 
   * <p>Calling this method releases any associated resources
   * to the given database handle.  The database handle should
   * not be used after a call to this method is made.
   * </p>
   * <p>
   * The freed-up resources include any save points, 
   * prepared statements, and opened log files.
   * The second parameter specifies if the
   * underlying database connection should be closed
   * as well or not.
   * </p>
   * 
   * @param db Database handle
   * @param closeConn Indicates 
   *                  if underlying connection should be closed.
   */
  public static void teardown(DB db, boolean closeConn) {
    db.teardown(CallInfo.create(), closeConn);
  }
  
  /**
   * Create a builder for a fresh data set.
   * @param source Data source.
   * @return A new data set builder.
   */
  public static DataSetBuilder builder(DataSource source) {
    return new DataSetBuilder(source);
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
   * 
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
   * Create a table builder.
   * 
   * <p>
   * A call to this method is equivalent to creating
   * a {@link TableBuilder} instance as follows:
   * </p>
   * <code>
   * &nbsp;&nbsp;new TableBuilder().name(tableName)
   * </code>
   * 
   * @param tableName Table name.
   * @return A new {@link Table} instance.
   */
  public static TableBuilder table(String tableName) {
    return new TableBuilder().name(tableName);
  }
  
  /**
   * Create a query data source from given SQL code.
   * @param db Database handle.
   * @param sql SQL code.
   * @param args Optional query arguments.
   * @return A new query data source.
   */
  @SafeVarargs
  public static Query query(DB db, String sql, Object... args) {
    return new Query(db, sql, args);
  }
  
  /**
   * Create a query builder.
   * <p>
   * A call to this method is equivalent to 
   * creating a {@link QueryBuilder} as follows:
   * </p>
   * <code>
   * &nbsp;&nbsp;new QueryBuilder().columns(columns)
   * </code>
   * 
   * @param columns Columns for query.
   * @return A new query builder.
   */
  @SafeVarargs
  public static QueryBuilder select(String... columns) {
    return new QueryBuilder().columns(columns);
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
   * but subsequent use of assertion methods does not require any book-keeping 
   * by the caller. Also, the returned data set is read-only (see {@link DataSet#isReadOnly()}).
   * </p>
   * 
   * @param source Data source.
   * @return Data set representing the snapshot.
   * 
   * @see #assertDelta(DataSet, DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see #assertUnchanged(DataSource)
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
   * Assert that two data sets are equivalent.
   * 
   * <p>
   * Note that the executed data set comparison is insensitive
   * to the order of rows in both data sets.
   * </p> 
   * 
   * @param expected Expected data.
   * @param actual Actual data.  
   * @throws DBAssertionError if the assertion fails.
   * @see #assertEquals(String,DataSet,DataSet)
   */
  public static void assertEquals(DataSet expected, DataSet actual) throws DBAssertionError {
    DBAssert.dataSetAssertion(CallInfo.create(), expected, actual);
  }
  
  /**
   * Assert that two data sets are equivalent (error message variant).
   * 
   * <p>
   * Note that the executed data set comparison is insensitive
   * to the order of rows in both data sets.
   * </p> 
   * 
   * @param message Assertion error message.
   * @param expected Expected data.
   * @param actual Actual data.  
   * @throws DBAssertionError if the assertion fails.
   * @see #assertEquals(DataSet,DataSet)
   */
  public static void assertEquals(String message, DataSet expected, DataSet actual) throws DBAssertionError {
    DBAssert.dataSetAssertion(CallInfo.create(message), expected, actual);
  }
  
  /**
   * Assert that no changes occurred for the given data source 
   * (error message variant).
   * 
   * @param message Assertion error message.
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
   * @param message Assertion error message.
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
   * @param message Assertion error message.
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
   * Assert that the given data source 
   * has no rows (error message variant).
   * 
   * <p>A call to this method is equivalent to
   * <code>assertState(message, empty(source))</code>.
   * 
   * @param message Assertion error message.
   * @param source Data source.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(String,DataSet)
   * @see #empty(DataSource)
   */
  public static void assertEmpty(String message, DataSource source) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(message), empty(source));
  }
  
  /**
   * Assert that the given data source has no rows.
   * 
   * <p>A call to this method is equivalent to
   * <code>assertState(empty(source))</code>.
   * @param source Data source.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(DataSet)
   * @see #empty(DataSource)
   */
  public static void assertEmpty(DataSource source) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(), empty(source));
  }
  
  /**
   * Assert that table exists in the database.
   * @param db Database.
   * @param tableName Table name.
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableDoesNotExist(DB, String)
   * @see #drop(Table)
   */
  public static void assertTableExists(DB db, String tableName) throws DBAssertionError {
    DBAssert.assertTableExistence(CallInfo.create(), db, tableName, true);
  }
  /**
   * Assert that table exists in the database (error message variant).
   * @param message Error message.
   * @param db Database.
   * @param tableName Table name. 
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableDoesNotExist(String, DB, String)
   * @see #drop(Table)
   */
  public static void assertTableExists(String message, DB db, String tableName) throws DBAssertionError {
    DBAssert.assertTableExistence(CallInfo.create(message), db, tableName, true);
  }
  
  /**
   * Assert that table does not exist in a database.
   * 
   * @param db Database.
   * @param tableName Table name. 
   * @throws DBAssertionError If the assertion fails.
   * 
   * @see #assertTableExists(DB,String)
   * @see #drop(Table)
   */
  public static void assertTableDoesNotExist(DB db, String tableName) throws DBAssertionError {
    DBAssert.assertTableExistence(CallInfo.create(), db, tableName, false);
  }
  
  /**
   * Assert that table does not exist in a database (error message variant).
   * @param message Error message.
   * @param db Database.
   * @param tableName Table. 
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableExists(String, DB, String)
   * @see #drop(Table)
   */
  public static void assertTableDoesNotExist(String message, DB db, String tableName) throws DBAssertionError {
    DBAssert.assertTableExistence(CallInfo.create(message), db, tableName, false);
  }
  
  /**
   * Insert a data set onto database.
   * 
   * @param data Data set for insertion.  
   */
  public static void insert(DataSet data) {
    DBSetup.insert(CallInfo.create(), data);
  }

  /**
   * Populate database with given data set.
   * 
   * @param data Data set for insertion.
   */
  public static void populate(DataSet data) {
    DBSetup.populate(CallInfo.create(), data);
  }

  /**
   * Populate database with given data set if the associated
   * table is seen as changed.
   * 
   * <p>A call to this method is functionally equivalent to
   * <br>
   * &nbsp;&nbsp;&nbsp;&nbsp;
   * <code>if ( changed(data.getSource()) ) populate(data);</code>
   * </p>
   * @param data Data set for insertion.
   * @see #changed(DataSource...)
   */
  public static void populateIfChanged(DataSet data) {
    DBSetup.populateIfChanged(CallInfo.create(), data);
  }
  
  /**
   * Delete all data from a table. 
   *
   * @param table Table.
   * @return Number of deleted entries.
   * @see #truncate(Table)
   * @see #deleteAllWhere(Table,String,Object...)
   */
  public static int deleteAll(Table table) {
    return DBSetup.deleteAll(CallInfo.create(), table);
  }
  
  /**
   * Drop a table (table handle variant). 
   * 
   * <p>
   * A call to this method is shorthand for <code>drop(table.getDB(), table.getName()))</code>.
   * </p>
   *
   * @param table Table to drop.
   * 
   * @see #drop(DB, String)
   * @see #assertTableExists(DB, String)
   * @see #assertTableDoesNotExist(DB, String)
   */
  public static void drop(Table table) {
    DBSetup.drop(CallInfo.create(), table.getDB(), table.getName());
  }

  
  /**
   * Drop a table. 
   * @param db Database.
   * @param tableName Table name.
   * 
   * @see #drop(Table)
   * @see #assertTableExists(DB, String)
   * @see #assertTableDoesNotExist(DB, String)
   */
  public static void drop(DB db, String tableName) {
    DBSetup.drop(CallInfo.create(), db, tableName);
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
   * @see #deleteAll(Table)
   * @see #truncate(Table)
   */
  @SafeVarargs
  public static int deleteAllWhere(Table table, String where, Object... args) {
    return DBSetup.deleteAll(CallInfo.create(), table, where, args);
  }

  /**
   * Truncate table.
   * @param table Table.
   * @see #deleteAll(Table)
   * @see #deleteAllWhere(Table,String,Object...)
   */
  public static void truncate(Table table) {
    DBSetup.truncate(CallInfo.create(), table);
  }
  
  /**
   * Set JDBDT save-point for database.
   * 
   * <p>
   * This method creates a save-point (through an internal 
   * {@link java.sql.Savepoint}) which can later
   * be restored (rolled back to) using {@link #restore(DB)}.
   * Note that JDBDT maintains only one save-point per database
   * handle, hence successive calls to this method discard the previous
   * save-point set (if any).
   * </p>
   * 
   * @param db Database handle.
   */
  public static void save(DB db)  {
    db.save(CallInfo.create());
  }

  /**
   * Restore database state to the last JDBDT save-point.
   * 
   * <p>
   * A call to this method restores (rolls back) the state 
   * to the last JDBDT save-point set using {@link #save(DB)}.
   * The save-point cannot be subsequently restored, i.e., only
   * one call to this method is allowed after each call to
   * {@link #save(DB)}.
   * </p>
   * 
   * @param db Database handle.
   */
  public static void restore(DB db) {
    db.restore(CallInfo.create());
  }

  /**
   * Commit changes in current transaction.
   * 
   * <p>
   * In database terms the method is simply a shorthand
   * for <code>db.getConnection().commit()</code>.
   * Any database save-points will be discarded,
   * including the JDBDT save-point, if set through 
   * {@link #save(DB)}.
   * </p>
   * 
   * @param db Database handle.
   * @see Connection#commit()
   */
  public static void commit(DB db) {
    db.commit(CallInfo.create());
  }

  /**
   * Check if given data sources are seen as changed.
   * 
   * <p>
   * A data source is marked as unchanged by a successful assertion
   * through 
   * {@link #assertUnchanged(DataSource)}
   * or {@link #assertUnchanged(String,DataSource)}. 
   * Every other setup or assertion method will mark the associated
   * data source as changed.
   * </p>
   * <p>
   * This method may be useful for conditional setup code that 
   * is executed only if the database needs to be reinitialized,
   * as illustrated below.
   * </p>
   * <pre>
   * DataSource ds = ...; 
   * 
   * if ( changed(ds) ) {
   *   // Setup initial state again
   *   ...
   * }
   * </pre>
   * 
   * @param dataSources Data sources.
   * @return <code>true</code> if at least one of the given data sources 
   *         is marked as changed.
   * @see #populateIfChanged(DataSet)
   */
  @SafeVarargs
  public static boolean changed(DataSource... dataSources) {
    if (dataSources == null || dataSources.length == 0) {
      throw new InvalidOperationException("No data sources specified");
    }
    for (DataSource ds : dataSources) {
      if (ds.getDirtyStatus()) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Dump the contents of a data set.
   * @param data Data set.
   * @param out Output stream.
   */
  public static void dump(DataSet data, PrintStream out) {
    try (Log log = Log.create(out);) {
      log.write(CallInfo.create(), data);
    }
  }
  
  /**
   * Dump the contents of a data set (output file variant).
   * 
   * <p>
   * The output file will be GZIP-compressed if it has a <code>.gz</code> extension.
   * </p>
   * 
   * @param data Data set.
   * @param outputFile Output file.
   */
  public static void dump(DataSet data, File outputFile) {
    try (Log log = Log.create(outputFile);) {
      log.write(CallInfo.create(), data);
    } 
  }
  
  /**
   * Dump the database contents for a data source.
   * 
   * @param source Data source.
   * @param out Output stream.
   */
  public static void dump(DataSource source, PrintStream out) {
    try (Log log = Log.create(out);) {
      log.write(CallInfo.create(), executeQuery(source));
    }
  }
  
  /**
   * Dump the database contents for a data source (file variant).
   *
   * <p>
   * The output file will be GZIP-compressed if it has a <code>.gz</code> extension.
   * </p>
   * 
   * @param source Data source.
   * @param outputFile Output file.
   */
  public static void dump(DataSource source, File outputFile) {
    try (Log log = Log.create(outputFile);) {
      log.write(CallInfo.create(), executeQuery(source));
    } 
  }
}

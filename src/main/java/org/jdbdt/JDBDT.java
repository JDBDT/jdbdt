/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
 * @since 1.0
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
   * @param dataSource Data source.
   * @return A new data set builder.
   */
  public static DataSetBuilder builder(DataSource dataSource) {
    return new DataSetBuilder(dataSource);
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
   * @param dataSource Data source.
   * @return A new data set for the given source.
   * @see DataSet#isEmpty()
   * @see DataSet#isReadOnly()
   */
  public static DataSet empty(DataSource dataSource) {
    return dataSource.theEmptySet();
  }

  /**
   * Create a new data set.
   * 
   * @param dataSource Data source.
   * @return A new data set for the given source.
   */
  public static DataSet data(DataSource dataSource) {
    return new DataSet(dataSource);
  }

  /**
   * Create a new typed data set.
   * @param <T> Type of objects.
   * @param dataSource Data source.
   * @param conv Conversion function.
   * @return A new typed data set for the given source.
   */
  public static <T> TypedDataSet<T> data(DataSource dataSource, Conversion<T> conv) {
    return new TypedDataSet<>(dataSource, conv);
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
   * @param dataSource Data source.
   * @return Data set representing the snapshot.
   * 
   * @see #assertDelta(DataSet, DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   * @see #assertUnchanged(DataSource)
   */
  public static DataSet
  takeSnapshot(DataSource dataSource)  {
    return dataSource.executeQuery(CallInfo.create(), true);
  }
  
  /**
   * Take a database snapshot for several data sources.
   * 
   * @param dataSources Data sources.
   * 
   * @see #takeSnapshot(DataSource)
   * @since 1.2
   */
  @SafeVarargs
  public static void
  takeSnapshot(DataSource... dataSources)  {
    foreach(dataSources, 
           (callInfo, dataSource) -> dataSource.executeQuery(callInfo, true), 
           CallInfo.create());
  }

  /**
   * Query the data source, without setting a snapshot.
   * @param dataSource Data source.
   * @return Query result.
   */
  static DataSet
  executeQuery(DataSource dataSource)  {
    return dataSource.executeQuery(CallInfo.create(), false);
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
   * @param dataSource Data source. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(String, DataSet)
   * @see #assertInserted(String, DataSet)
   */
  public static void assertUnchanged(String message, DataSource dataSource) throws DBAssertionError {
    DataSet emptyDataSet = empty(dataSource);
    DBAssert.deltaAssertion(CallInfo.create(message), emptyDataSet, emptyDataSet);
  }
  
  /**
   * Assert that no changes occurred for the given data sources 
   * (error message variant).
   * 
   * @param message Assertion error message.
   * @param dataSources Data sources. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(String,DataSource)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertUnchanged(String message, DataSource... dataSources) throws DBAssertionError {
    foreach(dataSources,
            (callInfo, dataSource) -> {
              DataSet emptyDataSet = empty(dataSource);
              DBAssert.deltaAssertion(callInfo, emptyDataSet, emptyDataSet);
            },
            CallInfo.create(message)); 
  }

  /**
   * Assert that no changes occurred for the given data source.
   * 
   * @param dataSource Data source. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(DataSet,DataSet)
   * @see #assertDeleted(DataSet)
   * @see #assertInserted(DataSet)
   */
  public static void assertUnchanged(DataSource dataSource) throws DBAssertionError {
    DataSet emptyDataSet = empty(dataSource);
    DBAssert.deltaAssertion(CallInfo.create(), emptyDataSet, emptyDataSet);
  }
  
  /**
   * Assert that no changes occurred for the given data sources.
   * 
   * @param dataSources Data sources. 
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(String,DataSource)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertUnchanged(DataSource... dataSources) throws DBAssertionError {
    foreach(dataSources,
            (callInfo, dataSource) -> {
              DataSet emptyDataSet = empty(dataSource);
              DBAssert.deltaAssertion(callInfo, emptyDataSet, emptyDataSet);
            },
            CallInfo.create()); 
  }

  /**
   * Assert that database changed only by removal of a given
   * data set (error message variant).
   * 
   * @param message The message for the assertion error.
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDelta(String, DataSet,DataSet)
   * @see #assertInserted(String, DataSet)
   * @see #assertUnchanged(String, DataSource)
   */
  public static void assertDeleted(String message, DataSet dataSet) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(message), dataSet, empty(dataSet.getSource())); 
  }
  
  /**
   * Assert that database changed by removal of given
   * data sets (error message variant).
   * 
   * @param message Assertion error message.
   * @param dataSets Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDeleted(String,DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertDeleted(String message, DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo, dataSet) -> 
               DBAssert.deltaAssertion(callInfo, dataSet, empty(dataSet.getSource())),
            CallInfo.create(message));
  }

  /**
   * Assert that database changed by removal of given
   * data set. 
   * 
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertInserted(DataSet)
   */
  public static void assertDeleted(DataSet dataSet) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(), dataSet, empty(dataSet.getSource())); 
  }

  /**
   * Assert that database changed by removal of given
   * data sets. 
   * 
   * @param dataSets Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertDeleted(DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertDeleted(DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo, dataSet) -> 
               DBAssert.deltaAssertion(callInfo, dataSet, empty(dataSet.getSource())),
            CallInfo.create());
  }
  
  /**
   * Assert that database changed only by addition of a given
   * data set (error message variant).
   * 
   * @param message Assertion error message.
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(String message, DataSet dataSet) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(message), empty(dataSet.getSource()), dataSet);
  }

  /**
   * Assert that database changed only by addition of given
   * data sets (error message variant).
   *
   * @param message Assertion error message.
   * @param dataSets Data sets.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertInserted(String,DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertInserted(String message, DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo,dataSet) -> DBAssert.deltaAssertion(callInfo, empty(dataSet.getSource()), dataSet),
            CallInfo.create(message));
  }
  
  /**
   * Assert that database changed only by addition of a given
   * data set.
   *
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertUnchanged(DataSource)
   * @see #assertDeleted(DataSet)
   * @see #assertDelta(DataSet,DataSet)
   */
  public static void assertInserted(DataSet dataSet) throws DBAssertionError {
    DBAssert.deltaAssertion(CallInfo.create(), empty(dataSet.getSource()), dataSet);
  }
  
  /**
   * Assert that database changed only by addition of given
   * data sets.
   *
   * @param dataSets Data sets.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertInserted(DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertInserted(DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo,dataSet) -> DBAssert.deltaAssertion(callInfo, empty(dataSet.getSource()), dataSet),
            CallInfo.create());
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
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   */
  public static void assertState(String message, DataSet dataSet) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(message), dataSet);
  }

  /**
   * Assert that the database state matches the given data sets (error message variant)
   * @param message Assertion error message.
   * @param dataSets Data sets.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(String,DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertState(String message, DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo, dataSet) -> DBAssert.stateAssertion(callInfo, dataSet),
            CallInfo.create(message));
  }
  
  /**
   * Assert that the database state matches the given data set.
   * @param dataSet Data set.
   * @throws DBAssertionError if the assertion fails.
   */
  public static void assertState(DataSet dataSet) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(), dataSet);
  }
  
  /**
   * Assert that the database state matches the given data sets.
   * @param dataSets Data sets.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(DataSet)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertState(DataSet... dataSets) throws DBAssertionError {
    foreach(dataSets,
            (callInfo, dataSet) -> DBAssert.stateAssertion(callInfo, dataSet),
            CallInfo.create());
  }

  /**
   * Assert that the given data source 
   * has no rows (error message variant).
   * 
   * <p>A call to this method is equivalent to
   * <code>assertState(message, empty(dataSource))</code>.
   * 
   * @param message Assertion error message.
   * @param dataSource Data source.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(String,DataSet)
   * @see #empty(DataSource)
   */
  public static void assertEmpty(String message, DataSource dataSource) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(message), empty(dataSource));
  }

  /**
   * Assert that the given data sources have no rows (error message variant).
   * @param message Assertion error message.
   * @param dataSources Data sources.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertEmpty(DataSource)
   * @see #assertState(DataSet...)
   * @see #empty(DataSource)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertEmpty(String message, DataSource... dataSources) throws DBAssertionError {
    foreach(dataSources,
            (callInfo, source) -> DBAssert.stateAssertion(callInfo, empty(source)),
            CallInfo.create(message));
  }
  
  /**
   * Assert that the given data source has no rows.
   * 
   * <p>A call to this method is equivalent to
   * <code>assertState(empty(source))</code>.
   * @param dataSource Data source.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertState(DataSet)
   * @see #empty(DataSource)
   */
  public static void assertEmpty(DataSource dataSource) throws DBAssertionError {
    DBAssert.stateAssertion(CallInfo.create(), empty(dataSource));
  }
  
  /**
   * Assert that the given data sources have no rows.
   * 
   * @param sources Data sources.
   * @throws DBAssertionError if the assertion fails.
   * @see #assertEmpty(DataSource)
   * @see #assertState(DataSet...)
   * @see #empty(DataSource)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertEmpty(DataSource... sources) throws DBAssertionError {
    foreach(sources,
            (callInfo, source) -> DBAssert.stateAssertion(callInfo, empty(source)),
            CallInfo.create());
  }

  /**
   * Assert that table exists in the database.
   * 
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
   * Assert that tables exist in the database.
   * 
   * @param db Database.
   * @param tableNames Table names.
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableDoesNotExist(DB, String...)
   * @see #drop(Table...)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertTableExists(DB db, String... tableNames) throws DBAssertionError {
    multipleTableAssertion(CallInfo.create(), db, tableNames, true);
  }
  
  /**
   * Assert that table exists in the database (error message variant).
   * 
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
   * Assert that tables exist in the database (error message variant).
   * 
   * @param message Assertion error message.
   * @param db Database.
   * @param tableNames Table names.
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableDoesNotExist(String, DB, String...)
   * @see #drop(Table...)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertTableExists(String message, DB db, String... tableNames) throws DBAssertionError {
    multipleTableAssertion(CallInfo.create(message), db, tableNames, true);
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
   * Assert that tables do not exist in a database.
   * 
   * @param db Database.
   * @param tableNames Table names. 
   * @throws DBAssertionError If the assertion fails.
   * 
   * @see #assertTableExists(DB,String...)
   * @see #drop(Table...)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertTableDoesNotExist(DB db, String... tableNames) throws DBAssertionError {
    multipleTableAssertion(CallInfo.create(), db, tableNames, false);
  }

  /**
   * Assert that table does not exist in a database (error message variant).
   * 
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
   * Assert that tables do not exist in a database (error message variant).
   * 
   * @param message Error message.
   * @param db Database.
   * @param tableNames Table names. 
   * @throws DBAssertionError If the assertion fails.
   * @see #assertTableExists(String, DB, String...)
   * @see #drop(Table...)
   * @since 1.2
   */
  @SafeVarargs
  public static void assertTableDoesNotExist(String message, DB db, String... tableNames) throws DBAssertionError {
    multipleTableAssertion(CallInfo.create(message), db, tableNames, false);
  }


  @SuppressWarnings("javadoc")
  private static void 
  multipleTableAssertion(CallInfo callInfo, DB db, String[] tableNames, boolean exists) {
    foreach(tableNames,
        (ci, name) -> DBAssert.assertTableExistence(ci, db, name, exists),
        callInfo); 
  }
  
  /**
   * Insert a data set into the database.
   * 
   * @param data Data set for insertion.  
   * @see #update(DataSet)
   * @see #delete(DataSet)
   * @see #populate(DataSet)
   * @see #populateIfChanged(DataSet)
   */
  public static void insert(DataSet data) {
    DBSetup.insert(CallInfo.create(), data);
  }
  
  /**
   * Insert data sets into the database.
   * 
   * @param dataSets Data sets for insertion.  
   * @see #insert(DataSet)
   * @see #delete(DataSet...)
   * @see #update(DataSet...)
   * @see #populate(DataSet...)
   * @see #populateIfChanged(DataSet...)
   * @since 1.2
   */
  @SafeVarargs
  public static void insert(DataSet... dataSets) {
    foreach(dataSets, DBSetup::insert, CallInfo.create());
  }

  /**
   * Update database according to given data set.
   * 
   * <p>The data set should be associated to a table
   * with defined key columns. The key column values
   * of each entry the data set determine the rows to be updated,
   * and the remaining column values are used in the update.
   * </p>
   * 
   * @param data Data set for update.  
   * @see TableBuilder#key(String...)
   * @see Table#getKeyColumns()
   * @see #update(DataSet...)
   * @see #insert(DataSet)
   * @see #populate(DataSet)
   * @see #populateIfChanged(DataSet)
   */
  public static void update(DataSet data) {
    DBSetup.update(CallInfo.create(), data);
  }
  
  /**
   * Update database according to given data sets.
   * 
   * <p>The data sets should be associated to tables
   * with defined key columns. The key column values
   * of each entry in a data set determine the rows to be updated,
   * and the remaining column values are used in the update.
   * </p>
   * 
   * @param dataSets Data sets for update.  
   * @see TableBuilder#key(String...)
   * @see Table#getKeyColumns()
   * @see #update(DataSet)
   * @see #insert(DataSet...)
   * @see #populate(DataSet...)
   * @see #populateIfChanged(DataSet...)
   * @since 1.2
   */
  @SafeVarargs
  public static void update(DataSet... dataSets) {
    foreach(dataSets, DBSetup::update, CallInfo.create());
  }


  /**
   * Delete data set from the database.
   * 
   * <p>The data set should be associated to a table
   * with defined key columns. The key column values
   * of each entry in the data set determine the rows to delete.
   * </p>
   * 
   * @param data Data set for deletion. 
   * @see TableBuilder#key(String...)
   * @see Table#getKeyColumns() 
   * @see #update(DataSet)
   * @see #insert(DataSet)
   * @see #populate(DataSet)
   * @see #populateIfChanged(DataSet)
   */
  public static void delete(DataSet data) {
    DBSetup.delete(CallInfo.create(), data);
  }

  /**
   * Delete data sets from the database.
   * 
   * <p>The data sets should be associated to tables
   * with defined key columns. The key column values
   * of each entry a data set determine the rows to delete.
   * </p>
   * 
   * @param dataSets Data sets for deletion. 
   * @see TableBuilder#key(String...)
   * @see Table#getKeyColumns() 
   * @see #update(DataSet...)
   * @see #insert(DataSet...)
   * @see #populate(DataSet...)
   * @see #populateIfChanged(DataSet...)
   * @since 1.2
   */
  @SafeVarargs
  public static void delete(DataSet... dataSets) {
    foreach(dataSets, DBSetup::delete, CallInfo.create());
  }


  /**
   * Populate database with given data set.
   * 
   * @param data Data set for insertion.
   * @see #populateIfChanged(DataSet)
   * @see #insert(DataSet)
   * @see #update(DataSet)
   * @see #delete(DataSet)
   */
  public static void populate(DataSet data) {
    DBSetup.populate(CallInfo.create(), data);
  }
  
  /**
   * Populate database with given data sets.
   * 
   * @param dataSets Data sets for insertion.
   * @see #populate(DataSet)
   * @see #populateIfChanged(DataSet)
   * @see #insert(DataSet...)
   * @see #update(DataSet...)
   * @see #delete(DataSet...)
   * @since 1.2
   */
  @SafeVarargs
  public static void populate(DataSet... dataSets) {
    foreach(dataSets, DBSetup::populate, CallInfo.create());
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
   * @see #populate(DataSet)
   * @see #insert(DataSet)
   * @see #update(DataSet)
   * @see #delete(DataSet)
   */
  public static void populateIfChanged(DataSet data) {
    DBSetup.populateIfChanged(CallInfo.create(), data);
  }
  
  /**
   * Populate database with given data sets if the associated
   * tables are seen as changed.
   * 
   * @param dataSets Data sets for insertion.
   * @see #populateIfChanged(DataSet)
   * @see #changed(DataSource...)
   * @see #populate(DataSet...)
   * @see #insert(DataSet...)
   * @see #update(DataSet...)
   * @see #delete(DataSet...)
   * @since 1.2
   */
  public static void populateIfChanged(DataSet... dataSets) {
    foreach(dataSets, DBSetup::populateIfChanged, CallInfo.create());
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
   * Delete all data from given tables. 
   *
   * @param tables Tables.
   * @see #deleteAll(Table)
   * @see #truncate(Table...)
   * @see #deleteAllWhere(Table,String,Object...)
   * @since 1.2
   */
  @SafeVarargs
  public static void deleteAll(Table... tables) {
    foreach(tables, DBSetup::deleteAll, CallInfo.create());
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
   * Drop tables (table handle variant). 
   * 
   * @param tables Tables to drop.
   * 
   * @see #drop(Table)
   * @see #drop(DB, String...)
   * @see #assertTableExists(DB, String)
   * @see #assertTableDoesNotExist(DB, String)
   * @since 1.2
   */
  @SafeVarargs
  public static void drop(Table... tables) {
    foreach(tables, 
        (callInfo, table) -> DBSetup.drop(callInfo, table.getDB(), table.getName()), 
        CallInfo.create());
  }

  /**
   * Drop a table (table name variant).
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
   * Drop tables. 
   * @param db Database.
   * @param tableNames Table names.
   * 
   * @see #drop(Table)
   * @see #drop(Table...)
   * @see #assertTableExists(DB, String)
   * @see #assertTableDoesNotExist(DB, String)
   * @since 1.2
   */
  @SafeVarargs
  public static void drop(DB db, String... tableNames) {
    foreach(tableNames, 
           (callInfo, tableName) -> DBSetup.drop(callInfo, db, tableName), 
           CallInfo.create());
  }

  /**
   * Delete all data from a table, subject to a <code>WHERE</code>
   * clause. 
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
   * Truncate tables.
   * @param tables Tables to truncate.
   * @see #truncate(Table)
   * @see #deleteAll(Table...)
   * @see #drop(Table...)
   * @since 1.2
   */
  @SafeVarargs
  public static void truncate(Table... tables) {
    foreach(tables, DBSetup::truncate, CallInfo.create());
  }

  /**
   * Execute arbitrary SQL for a database instance.
   * 
   * <p>
   * You may use this method for arbitrary database setup
   * actions.
   * </p>
   * 
   * @param db Database.
   * @param sql SQL statement.
   * @param args SQL statement arguments.
   * @return The value obtained through {@link PreparedStatement#getUpdateCount()}, after executing the statement.
   * @since 1.1
   */
  public static int execute(DB db, String sql, Object... args) {
    return DBSetup.execute(CallInfo.create(), db, sql, args);
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
   * @see #restore(DB)
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
   * @see #save(DB)
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
   * Check if given data source is seen as changed.
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
   * Table t = ...; 
   * 
   * if ( changed(t) ) {
   *   // Setup initial state again
   *   ...
   * }
   * </pre>
   * 
   * @param dataSource Data sources.
   * @return <code>true</code> if the data source is marked as changed.
   * @see #changed(DataSource...)
   * @see #populateIfChanged(DataSet)
   * @since 1.2
   */
  public static boolean changed(DataSource dataSource) {
    if (dataSource == null ) {
      throw new InvalidOperationException("Null data source specified!");
    }
    return dataSource.getDirtyStatus();
  }
  
  /**
   * Check if given data sources are seen as changed.
   * 
   * @param dataSources Data sources.
   * @return <code>true</code> if at least one of the given data sources 
   *         is marked as changed.
   * @see #changed(DataSource)
   * @see #populateIfChanged(DataSet...)
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
    try (Log log = Log.create(out)) {
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
    try (Log log = Log.create(outputFile)) {
      log.write(CallInfo.create(), data);
    } 
  }

  /**
   * Dump the database contents for a data source.
   * 
   * @param dataSource Data source.
   * @param out Output stream.
   */
  public static void dump(DataSource dataSource, PrintStream out) {
    try (Log log = Log.create(out)) {
      log.write(CallInfo.create(), executeQuery(dataSource));
    }
  }

  /**
   * Dump the database contents for a data source (file variant).
   *
   * <p>
   * The output file will be GZIP-compressed if it has a <code>.gz</code> extension.
   * </p>
   * 
   * @param dataSource Data source.
   * @param outputFile Output file.
   */
  public static void dump(DataSource dataSource, File outputFile) {
    try (Log log = Log.create(outputFile)) {
      log.write(CallInfo.create(), executeQuery(dataSource));
    } 
  }

  /**
   * Auxiliary type for {@code JDBDT#varArgsCall(VAHandler)}.
   *
   * @param <T> Type of objects.
   */
  private interface CallHandler<T> {
    /** 
     * Execution action. 
     * @param callInfo Call information.
     * @param object Argument for handler.
     */
    void action(CallInfo callInfo, T object);
  }

  /**
   * Utility method to deal with facade methods that accept 
   * variable length arguments. 
   * @param arguments Argument array.
   * @param handler handler.
   * @param callInfo Call info.
   * @param <T> Type of objects.
   */
  private static <T> void foreach(T[] arguments, CallHandler<T> handler, CallInfo callInfo) {
    if (arguments == null || arguments.length == 0) {
      throw new InvalidOperationException("Empty or null array!");
    }
    for (T argument : arguments) {
      handler.action(callInfo, argument);
    }
  }
}

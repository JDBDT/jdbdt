# Database setup

The contents of a database may be defined using setup methods 
in the `JDBDT` facade. The functionality at stake comprises
[inserting data / populating tables](DBSetup.html#Insert), 
[cleaning up data from tables](DBSetup.html#Clean), 
and [setting and restoring a save-point](DBSetup.html#SaveAndRestore).
A number of [database setup patterns](DBSetup.html#Patterns) can be
implemented using these operations.


	  
## Inserting data
<a name="Insert"></a>

Database data may be inserted using one of the following methods:

1. `insert(data)` inserts `data` (a [data set](DataSets.html)) into the [table](DataSources.html) given by `data.getSource()`.
2. `populate(data)` inserts `data` like `insert`, but clears the table first using a `DELETE` statement,
and also records `data` as the [snapshot for subsequent delta assertions](DBAssertions.html#Snapshots).

Thus, `insert` should be used for incremental additions to a table, whereas
`populate` should be used to reset the contents of a table contents entirely. 
The use of `populate` is more adequate in particular if [delta assertions](DBAssertions.html) 
are performed over the table subsequently.



*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    import java.sql.Date;
    ...
	DB db = ...;
	Table t = ...	
    
    // Create a data set for t.
    DataSet data = 
       builder(t)
      . ...
      .data();
    ...
    
    // 1. Reset contents of USER to data.
    populate(data); 
    ...
    
    // 2. OR insert data in USER table (does not clear previous contents).
    insert(data);

The `populateIfChanged` method is a variant of `populate` that 
executes conditionally, i.e., if the table contents are seen as unchanged,no operation takes place. This only happens if an `assertUnchanged` [assertion](DBAssertions.html) previously succeeded,
and no intervening subsequent JDBDT setup or assertion methods were called for the table. 

More generally, you may query the changed status of data sources using the `changed` facade method, and use it to guide database setup if convenient.

## Cleaning data
<a name="Clean"></a>

Database data may be cleaned up using one of the following methods for a `Table` instance `t`:

1. `deleteAll(t)` clears  the entire contents of table `t` using a `DELETE` statement without an associated `WHERE` clause.
2. `deleteAllWhere(t, whereClause, [,args])` clears the contents of `t` using a `DELETE` 
statement with the specified `WHERE` clause  (`whereClause`) and optional `WHERE` clause arguments `args`.
3. `truncate(t)` clears `t` using a `TRUNCATE TABLE` statement.
4. `drop(t)` or `drop(db, tableName)` drops the entire table.

*Note*: `truncate` may be faster than `deleteAll`, but the associated TRUNCATE TABLE statement 
may not respect integrity constraints and has variable semantics 
for different database engines (e.g., <a href="https://en.wikipedia.org/wiki/Truncate_(SQL)">see here</a>). Some engines do not support table truncation altogether (for instance SQLite).

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table t = table("USERS")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	         .build(db);
	...
	// 1. Clear table using a DELETE statement.
	deleteAll(t);
	
	// 2. Delete all users whose login matches a certain filter
	String loginFilter = ...;
	deleteAll(t, "LOGIN LIKE ?", loginFilter);
	
	// 3. Clear table using TRUNCATE.
	truncate(t);
	
	// 4. Drop the table entirely.
	drop(t);   // alternatively: drop(db, "USERS")

## Save and restore
<a name="SaveAndRestore"></a>

Database state may be saved and restored as follows per [database handle](DB.html) `db`:

1. A call to `save(db)` sets a database save-point. Internally, the 
save-point is set `java.sql.Connection.setSavepoint()` 
for the underlying database connection, which must have auto-commit
disabled. 
2. A call to `restore(db)` restores (rolls back) the database state to the 
JDBDT save-point defined using the last call to `save(db)`,
as long as there were no intervening database commits.

Note that that an unique one save-point is maintained per database handle,
and that there should be exactly one `restore` call per each `save` call. 
These constraints try to ensure portable behavior across database engines.

In relation to `save` and `restore`, `commit(db)` is a shorthand for
`db.getConnection().commit()`. Such a call commits all database changes
and discards the JDBDT save-point (or any other save-point set for the database otherwise, e.g., by the SUT itself).

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import java.sql.Connection;
    import org.jdbdt.DB;
    ...
    // Database handle ...
    DB db = database(...);
    // Disable auto-commit
    db.getConnection().setAutoCommit(false);
    
    // Set save-point
    save(db);
    
    // Exercise the SUT, then execute some assertions  
    letTheSUTWork();
    assertXXX();
    
    // Restore database state
    restore(db);

    
## Database setup patterns

A number of database test patterns can be implemented using JDBDT, as exemplified in the [JDBDT tutorial](Tutorial.html). The code skeleton below (assuming [JUnit](http://junit.org)-based tests) 
illustrates the implementation of two patterns described in [xunitpatterns.com](http://xunitpatterns.com):

1. [Transaction Rollback Teardown](http://xunitpatterns.com/Transaction%20Rollback%20Teardown.html):
changes to the database are rolled back at the end of each test, back to an initial configuration. In the illustration below, the reference database state is set once in `oneTimeSetup` (annotated with `@BeforeClass`). This state is respectively saved and restored, before and after each test executes,
in `setSavePoint` (annotated with `@Before`) and `restoreSavePoint` (annotated with `@After`).
2. [Table Truncation Teardown](http://xunitpatterns.com/Table%20Truncation%20Teardown.html):
clean up each table on tear-down after conducting tests, as shown for `oneTimeTeardown` (annotated
with `@AfterClass`).

*Illustration* 

	import java.sql.Connection;
	import org.junit.BeforeClass;
	import org.junit.AfterClass;
    import org.junit.Before;
	import org.junit.After;
	import org.junit.Test;

	import static org.jdbdt.JDBDT.*;
	import org.jdbdt.DB;
	import org.jdbdt.DataSet;
	import org.jdbdt.Table;
	
	public class MyTest {
	
	  static DB myDB;
	  static Table myTable1, myTable2, ... ;
	  
	  @BeforeClass 
	  public static void oneTimeSetup() {
	    ...
	    // Setup database handle
	    myDB = database( ... );
	    
	    // Define tables and corresponding initial data
	    myTable1 = table(...) ... 
	    DataSet initialData1 = data(myTable1). ... ;
	    populate(initialData1);
	 
	    // etc for myTable2 ...
	    myTable2 = table(...) ...;
	    ...
	    
	    // Ensure that auto-commit is off
	    myDB.getConnection().setAutoCommit(false);
	  }
	  
	  @AferClass
	  public void oneTimeTeardown() {
	    // Alternatively use deleteAll ...
	    truncate(myTable1);
	    truncate(myTable2);
	    ...
	    teardown(myDB, true); // free resources and close DB connection 
	  }
	  
	  @Before
	  public void setSavePoint() {
	    save(myDB);
	  }
	  
	  @After
	  public void restoreSavePoint() {
	    restore(myDB);
	  }
	  
	  @Test 
	  public void test1() {
	    // Specific setup for test
	    ...
	    // Exercise the SUT, perform assertions
	    ...
	  }
	  
	  @Test 
	  public void test2() { ... etc ... } 
	  ...
	 
	  
## Summary of methods
<a name="MethodReference"></a>

### `JDBDT`

Insertion:

- `insert(data)` inserts `data` into a table (the table is `data.getSource()`).
- `populate(data)` sets `data` as the contents of a table (the table is `data.getSource()`).
- `populateIfChanged(data)` sets `data` as the contents of a table, if the table is perceived as having changed.

Clean-up:

- `delete(t)` clear table `t` with a DELETE statement.
- `deleteAll(t,w,a)` deletes data from table `t` subject to WHERE clause `w` and optional
WHERE clause arguments.
- `truncate(t)` clear table `t` with a TRUNCATE TABLE statement.

Save and restore:

- `save(db)` sets the JDBDT save-point;
- `restore(db)` restores database state back to the JDBDT save-point;
- `commit(db)` performs a database commit, discarding the JDBDT save-point (or any other save-point set);

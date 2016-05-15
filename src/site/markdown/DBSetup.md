# Database setup

The contents of a database may be defined using setup methods 
in the `JDBDT` facade. The functionality at stake comprises [cleaning up data from tables](DBSetup.html#Clean), [inserting data / populating tables](DBSetup.html#Insert), 
and [setting and restoring a save-point](DBSetup.html#SaveAndRestore).
A number of [database setup patterns](DBSetup.html#Patterns) can be
implemented using these operations.

## Cleaning data
<a name="Clean"></a>

Database data may be cleaned up using one of the following methods:

1. `deleteAll(t)` clears  the entire contents of table `t` using a DELETE statement without an associated
WHERE clause.
2. `deleteAllWhere(table, where, [,args])` clears the contents of `t` using a DELETE
statement with a WHERE clause `where` and optional WHERE clause arguments `args`.
3. `truncate(t)` clears `t` using a TRUNCATE TABLE statement.

Note that `truncate` may  be faster than `deleteAll`, but the associated TRUNCATE TABLE statement 
may not respect integrity constraints and has variable semantics 
for different database engines; check some details [here](https://en.wikipedia.org/wiki/Truncate_(SQL). 
Also, the TRUNCATE TABLE statement is [not supported](Compatibility.html#KnownIssues) at all by some database engines.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table t = table(db, "USER")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	...
	// 1. Clear table using a DELETE statement.
	deleteAll(t);
	
	// 2. Delete all users whose login matches a certain filter
	String loginFilter = ...;
	deleteAll(t, "LOGIN LIKE ?", loginFilter);
	
	// 3. Clear table using TRUNCATE.
	truncate(t);
	  
## Inserting data
<a name="Insert"></a>

Database data may be inserted using one the following methods:

1. `insert(data)` inserts `data` into the table given by `data.getSource()`.
2. `populate(data)` inserts `data` like `insert`, but clears the table first using a DELETE statement,
and also records `data` as the [snapshot for subsequent delta assertions](DBAssert.html#Snapshots).

Thus, `insert` should be used for incremental additions to a table, whereas
`populate` should be used to reset the contents of a table contents entirely. 
The use of `populate` is adequate in particular if [delta assertions](DBAssert.html) 
are performed over the table subsequently.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    import java.sql.Date;
    ...
	DB db = ...;
	Table t = table(db, "USER")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");	
    
    // Create a data set with 500 rows.
    DataSet data = 
       builder(t)
      .sequence("ID", 0) // 0, 1, ... 
      .sequence("LOGIN", i -> "user_" + i) // "user_0", ...
      .sequence("NAME", i -> "Yet Another User" + i) 
      .sequence("PASSWORD", i -> "password_" + i) 
      .random("CREATED", Date.valueOf("2016-01-01"), Date.valueOf("2016-12-31"))
      .generate(500);
    ...
    
    // 1. Reset contents of USER to data.
    populate(data); 
    ...
    
    // 2. Insert data in USER table (does not clear previous contents).
    insert(data);

## Save and restore
<a name="SaveAndRestore"></a>

Database state may be saved and restored as follows per [database handle](DB.html) `db`:

1. A call to `save(db)` sets a database save-point. Internally, the 
save-point is set `java.sql.Connection.setSavepoint()` 
for the underlying database connection, which must have auto-commit
disabled. Nested save points are not supported for simplicity,
i.e., only one save-point is maintained per database handle.
2. A call to `restore(db)` restores (rolls back) the database state corresponding to the 
last JDBDT save-point  defined using `save(db)`,
as long as there were no intervening database commits.

In relation to `save` and `restore`, `commit(db)` is a shorthand for
`db.getConnection().commit()`. Such a call commits all database changes
and discards the JDBDT save-point (or any other save-point set otherwise) .

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import java.sql.Connection;
    import org.jdbdt.DB;
    ...
    // Database handle ...
    // The associated connection should have auto-commit disabled.
    Connection conn = ...;
    conn.setAutoCommit(false);
    DB db = database(conn);
    
    // Set save-point
    save(db);
    
    // Exercise the SUT, then execute some assertions  
    letTheSUTWork();
    assert...();
    
    // Restore database state
    restore(db);

    
## Database setup patterns

A number of database test patterns can be implemented using JDBDT. 
For instance, in the illustration below (assuming [JUnit](http://junit.org)-based tests) 
provides a skeleton for the implementation of two patterns described in [xunitpatterns.com](http://xunitpatterns.com):

1. [Transaction Rollback Teardown](http://xunitpatterns.com/Transaction%20Rollback%20Teardown.html):
changes to the database are rolled back at the end of each test, back to an initial configuration. In the illustration below, the initial database state is set once in `oneTimeSetup` (annotated with `@BeforeClass`) and restored in `perTestTeardown` (annotated with `@After`) after each test.
2. [Table Truncation Teardown](http://xunitpatterns.com/Table%20Truncation%20Teardown.html):
clean up each table on tear-down after conducting tests, as shown for `oneTimeTeardown` (annotated
with `@AfterClass`) in the illustration below.

*Illustration* 

	import java.sql.Connection;
	import org.junit.BeforeClass;
	import org.junit.AfterClass;
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
	    Connection c = ...;
	    c.setAutoCommit(false);
	    myDB = database(c);
	    
	    // Define tables and corresponding initial data
	    myTable1 = table("...").columns(...);
	    DataSet initialData1 = data(myTable1). ... ;
	    populate(initialData1);
	 
	    // etc for myTable2 ...
	    myTable2 = table("...").columns(...);
	    ...
	    
	    // Set save-point
	    save(myDB);
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
	  
	  @After
	  public void perTestTeardown() {
	    restore(myDB);
	  }
	  
	  @AferClass
	  public void oneTimeTeardown() {
	    // Alternatively use deleteAll ...
	    truncate(myTable1);
	    truncate(myTable2);
	    ...
	  }
	  
## Summary of methods
<a name="MethodReference"></a>

### `JDBDT`

Clean-up:

- `delete(t)` clear table `t` with a DELETE statement.
- `deleteAll(t,w,a)` deletes data from table `t` subject to WHERE clause `w` and optional
WHERE clause arguments.
- `truncate(t)` clear table `t` with a TRUNCATE TABLE statement.

Insertion:

- `insert(data)` inserts `data` into a table (`data.getSource()`).
- `populate(data)` sets `data` as the contents of a table (`data.getSource()`).

Save and restore:

- `save(db)` sets the JDBDT save-point;
- `restore(db)` restores database state back to the JDBDT save-point;
- `commit(db)` performs a database commit, discarding the JDBDT save-point (or any other set);

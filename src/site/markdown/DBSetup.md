# Database setup

The contents of a database may be defined using setup methods 
in the `JDBDT` facade. The functionality at stake comprises: 

* the use of [data sets](DataSets.html) to [populate a table](DBSetup.html#Populate)
but also for table row [insertions / updates / deletions](DBSetup.html#IUD);

* [cleaning up database tables](DBSetup.html#Clean);

* and [setting and restoring save-points](DBSetup.html#SaveAndRestore).

These functionalities are described below, along with a discussion of a few   [database setup patterns](DBSetup.html#Patterns)  that can be implemented using these operations.

<a name="Populate"></a>
## Populating a table


The `populate` method may be used to populate a database table. Taking a data set for a table as argument, it first clears the table at stake, then 
inserts the data set into the table. The supplied data set also sets a snapshot for [subsequent delta assertions](DBAssert.html#DeltaAssertions).

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    ...
	DB db = ... 
	Table t = ...	
    
    // Create a data set for t.
    DataSet initialData = data(t) ... 
                   // or ... builder(t) for instance
    
    populate(initialData); 
    

The `populateIfChanged` method is a variant of `populate` that 
executes conditionally, i.e., if the table contents are seen as unchanged, no operation takes place. This only happens if an `assertUnchanged` [assertion](DBAssertions.html) previously succeeded,
and no intervening subsequent JDBDT setup or assertion methods were called for the table. 

*Illustration* 

    static Table theTable ;
    static DataSet initialStata; 
    
    @BeforeClass
    public static void globalSetup() {
      theTable = ... ;
      initialData = data(theTable) ...
    }
    
    @Before
    public void perTestSetup() {
       populateIfChanged(initialData);
    }
    
    @Test
    public void test1() {
      theSUT.methodThatShouldNotChangeAnythin();
      assertUnchanged(theTable);
      // populateIfChanged will do nothing if the assertion succeeds
    }
    
    @Test
    public void test1() {
      theSUT.methodThatPerformsChanges();
      assertXXX(...); // any other assertion method
      // populateIfChanged will repopulate the table again,
      // regardless of whether the assertion succeeds or not
    }

More generally, you may query the changed status of data sources using the `changed` facade method, and use it to guide database setup if convenient.

*Illustration* 

    @Before
    public void perTestSetup() {
       if (changed(theTable)) {
         populate(initialData); // re-populate
         ...   // other necessary setup actions
       }
    }
 

<a name="IUD"></a>
## Data set insertions, updates and deletes 

Beyond `populate`, data sets may be used for table insertions, updates and deletes.

The `insert`  method inserts a given data set onto a table, without deleting any previous contents (unlike `populate` that clears the table first).  

    Table t = ...	
    DataSet additionalData = data(t) ... 
    insert(additionalData);

<a name="DataSetUpdate"></a>
<a name="DataSetDelete"></a>

The `update` and `delete` method respectively update and delete a data set in the database. They require that [key columns](DataSources.html#Table) are defined for the table at stake. The corresponding key values for each data set element will determine which rows are to be updated / deleted.

    DB db = ...; 
    Table t = table("MyTable")
             .columns( ... )
             .key( ... )
             .build(db);	
    DataSet ds = ... 
    
    // Update
    update(ds);  
    
    // Delete
    delete(ds);


<a name="Clean"></a>
## Cleaning a table


Database data may be cleaned up using one of the following methods for a `Table` instance `t`:

1. `deleteAll(t)` clears  the entire contents of table `t` using a `DELETE` statement without an associated `WHERE` clause.
2. `deleteAllWhere(t, whereClause, [,args])` clears the contents of `t` using a `DELETE` 
statement with the specified `WHERE` clause  (`whereClause`) and optional `WHERE` clause arguments `args`.
3. `truncate(t)` clears `t` using a `TRUNCATE TABLE` statement.
4. `drop(t)` or `drop(db, tableName)` drops a table entirely.

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

<a name="SaveAndRestore"></a>
## Saving and restoring database state 


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


<a name="Patterns"></a>
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
	  
	  @AfterClass
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

Operations using a data set `data` defined for a table `t` (`t` should correspond to `data.getSource()`):

- `populate(data)` sets `data` as the contents of a `t`.
- `populateIfChanged(data)` sets `data` as the contents of `t`, if `t` is perceived as having changed.
- `insert(data)` inserts `data` into `t`.
- `delete(data)` deletes `data` from `t`.
- `update(data)` uses `data` to update `t`.

Clean-up:

- `delete(t)` clear table `t` with a DELETE statement.
- `deleteAll(t,w,a)` deletes data from table `t` subject to WHERE clause `w` and optional
WHERE clause arguments.
- `truncate(t)` clear table `t` with a TRUNCATE TABLE statement.

Save and restore:

- `save(db)` sets the JDBDT save-point;
- `restore(db)` restores database state back to the JDBDT save-point;
- `commit(db)` performs a database commit, discarding the JDBDT save-point (or any other save-point set);

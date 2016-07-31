# Tutorial

This tutorial helps you understand how to use the essential
features of JDBDT.  It assumes that you are reasonably familiar with [Maven](http://maven.org) and [JUnit](http://junit.org), since that the tutorial code is organized as a Maven project (you can download), and that JUnit tests are used as illustration.

**Contents**

* 	[Tutorial code](Tutorial.html#TheCode)
	*	[Getting the code](Tutorial.html#TheCode.GetIt)
	*	[Maven project overview](Tutorial.html#TheCode.MavenProject)
	*   [Running the tests](Tutorial.html#TheCode.RunningTheTests)
* 	[Test subject](Tutorial.html#TheTestSubject)
	*	[The USERS table](Tutorial.html#TheTestSubject.Table)
	*	[The User class](Tutorial.html#TheTestSubject.UserClass)
	*	[The UserDAO class](Tutorial.html#TheTestSubject.UserDAOClass)
* 	[Test code](Tutorial.html#TheTestCode)
	*	[Dababase setup](Tutorial.html#TheTestCode.DBSetup)
	*	[Dababase teardown](Tutorial.html#TheTestCode.DBTeardown)
	*	[Per-test setup and teardown](Tutorial.html#TheTestCode.PerTestSetupAndTeardown)
	
## Tutorial code
<a name="#TheCode"></a>

### Getting the code
<a name="TheCode.GetIt"></a>

You may clone the tutorial code from [the github repository](http://github.com/edrdo/jdbdt-tutorial):
	
	git clone git@github.com:edrdo/jdbdt-tutorial.git

### Maven project overview
<a name="TheCode.MavenProject"></a>

The code is organized as a Maven project that comprises the following artifacts:

1. A simple database containing a single table called `USERS`
(in `src/main/resources/tableCreation.sql`).	
2. `User`, a POJO class to store user data (`src/main/java/org/jdbdt/tutorial/User.java`)
3. `UserDAO`,  a data-access object (DAO) class for user data (`src/main/java/org/jdbdt/tutorial/UserDAO.java`);
4. `UserDAOTest`, a class containing JUnit tests for `UserDAO`, making use of JDBDT (`src/test/java/org/jdbdt/tutorial/UserDAOTest.java`).
This class will be our main point of interest.
5. Subclasses of `UserDAOTest`, that merely configure the database driver to use.
There are three such classes `DerbyTest`, `H2Test`, `HSQLDBTest` in `src/test/java/org/jdbdt/tutorial`. As their name indicates, they make use of JDBC drivers for [Apache Derby](http://db.apache.org/derby), [H2](http://h2database.com), and [HSQLDB](http://hsqldb.org). These test classes are grouped in the `AllTests` JUnit test suite class in the same directory.

### Running the tests
<a name="TheCode.RunningTheTests"></a>

In the command line, go to the root folder of the project, and type `mvn test`.  
This will execute the `AllTests` suite.  

Otherwise, import the project using a Maven-compatible IDE.  
[Eclipse](http://eclipse.org) users will find that a `.project` file is already in the root folder ([M2Eclipse plugin required](http://www.eclipse.org/m2e/)).

## The test subject
<a name="TheTestSubject"></a>

The SUT of the tutorial is a `UserDAO` class. Objects of this kind 
works as a data-access object for a database table called `USERS`,
whose Java representation is given by the POJO `User` class. 
These items are described below.

### The `USERS` table 
<a name="TheTestSubject.Table"></a>

The `USERS` table represents user data in the form of a numeric id (primary key), a unique login, a name, a password, a role, and a creation date. The code for table creation below should be self-explanatory.  A sequence or identity column setting could be associated to the `ID` column, but we keep the example as simple as possible to ensure portability for different database engines. Likewise, for `ROLE`, a reference table or an `ENUM` type (as supported by some engines) could be used alternatively.

	CREATE TABLE USERS 
	(
		ID INTEGER PRIMARY KEY NOT NULL ,
		LOGIN VARCHAR(16) UNIQUE NOT NULL,
		NAME VARCHAR(32),
		PASSWORD VARCHAR(32) NOT NULL,
		ROLE VARCHAR(7) DEFAULT 'REGULAR' NOT NULL
		  CHECK (ROLE IN ('ADMIN', 'REGULAR', 'GUEST')),
		CREATED DATE NOT NULL
	)
	
### The `User` class
<a name="TheTestSubject.UserClass"></a>

The `User` class is a POJO class with getter and setter methods for each of the user attributes (e.g., `getId()`, `setId`). Additionally, it overrides a number of `java.lang.Object` methods for convenience of use in test code (e.g., `equals`). 

### The `UserDAO` class
<a name="TheTestSubject.UserDAOClass"></a>

The `UserDAO` class defines methods for interfacing with the `USERS` table 
using `User` objects. The methods are correspondence to database operations
for user insertion, update, removal and retrieval.

* `insertUser(u)`: inserts a new user.
* `updateUser(u)`: update an existing user.
* `deleteUser(u)`: delete a user.
* `deleteAllUsers()`: delete all users.
* `getUser(id)`: get user data by id.
* `getUser(login)`: get user data by login.
* `getAllUsers()`: get a list of all users.
* `getUsers(r)`: get a list of all users with role `r`.

## Test code
<a name="TheTestCode"></a>

The test code of `UserDAOTest` makes use of JDBDT to setup and validate the
contents of the database. You may notice the following JDBDT imports:

	import static org.jdbdt.JDBDT.*; 

	import org.jdbdt.Conversion;
	import org.jdbdt.DB;
	import org.jdbdt.DataSet;
	import org.jdbdt.Table;

The static import (the very first one) relates to methods in the [JDBDT facade](Facade.html) that exposes the core JDBDT API.

### Database setup 
<a name="TheTestCode.DBSetup"></a>

To setup the database connection and define the initial contents of the database,
each subclass of `UserDAOTest` defines a `globalSetup`
method that is executed once before all tests, since it is marked with the `@BeforeClass` JUnit annotation. 

Each of these methods merely calls `UserDAO.globalSetup(dbDriverClass,dbURL)`, parameterizing the JDBC driver class to load and the database URL to use for the actual setup. For instance, `DerbyTest` contains:

	private static final String 
    	JDBC_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String 
    	DATABASE_URL = "jdbc:derby:./db/derby/jdbdtTutorial;create=true";
    	
    @BeforeClass
    public static void globalSetup() throws Throwable {
      globalSetup(JDBC_DRIVER_CLASS, DATABASE_URL);
    }
 
This organization is merely a convenient one for the purpose of 
testing multiple JDBC drivers in the tutorial code.
In `UserDAO` we have
    
    protected static
    void globalSetup(String jdbcDriverClass, String databaseURL) ... { 
      ... 
    }

that proceeds in the following steps:

- We first ensure that the JDBC driver class is loaded.

    // Load JDBC driver class
    Class.forName(jdbcDriverClass);
    
- The JDBDT [database handle](DB.html) is then created.
   
    // Create database handle
    theDB = database(databaseURL);
 
- So is the `UserDAO` instance, our SUT, along with the `USERS` table
(JDBDT provides no facilities to create the table itself) ...
   
    // Create DAO and in turn let it create USERS table 
    theDAO = new UserDAO(theDB.getConnection());
    theDAO.createTable();

- ... and a JDBDT `Table` [data source](DataSources.html) for the `USERS` table. 
   
    // Create table data source.
    theTable = table(theDB, "USERS")
              .columns("ID",
                       "LOGIN", 
                       "NAME", 
                       "PASSWORD",
                       "ROLE",
                       "CREATED" );
                       
- ... plus, finally, the [data set](DataSets.html) for the initial contents of the database. The strategy in this case is to use a [data set builder](DataSets.html#Creation.Builder). 
We populate the database with 1 `ADMIN` user, 3 `REGULAR` users, and 2 `GUEST`
users. The data set builder facilities allow for a succinct definition of the data, which is as follows:

    // Define data set for populating the database
    theInitialData
      =  builder(theTable)
        .sequence("ID", 0)
        .value("LOGIN", "root")
        .sequence("PASSWORD", i -> "pass" + i)
        .nullValue("NAME")
        .value("CREATED", FIXED_DATE)
        .value("ROLE", ADMIN)
        .generate(1)
        .sequence("LOGIN", "alice", "bob", "charles")
        .sequence("NAME",  "Alice", "Bob", "Charles")
        .value("ROLE", REGULAR)
        .generate(3)
        .sequence("LOGIN", i -> "guest" + i, 1)
        .sequence("NAME",  i -> "Guest User " + i, 1)
        .value("ROLE", GUEST)
        .generate(2)
        .data();
      // debug(theInitialData, System.err);

Uncomment the last statement above, a call to `debug`, if you wish to see some [debug output](Logs.html) sent to `System.err` describing the data set. The following table summarizes the created entries (note that `FIXED_DATE` equals `2016-01-01`):

<table border="1">
	<tr>
		<th align="left">
			<code>ID</code>
		</th>
		<th align="left">
		  	<code>LOGIN</code>
		</th>
	    <th align="left">
		  	<code>NAME</code>
		</th>
		<th align="left">
		  	<code>PASSWORD</code>
		</th>
		<th align="left">
		  	<code>ROLE</code>
		</th>
		<th align="left">
		  	<code>CREATED</code>
		</th>
	</tr>
	<tr>
		<td align="left">
			<code>0</code>
		</td>
		<td align="left">
		  	<code>root</code>
		</td>
	    <td align="left">
		  	<code>NULL</code>
		</td>
		<td align="left">
		  	<code>pass0</code>
		</td>
		<td align="left">
		  	<code>ADMIN</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
	<tr>
		<td align="left">
			<code>1</code>
		</td>
		<td align="left">
		  	<code>harry</code>
		</td>
	    <td align="left">
		  	<code>Harry</code>
		</td>
		<td align="left">
		  	<code>pass1</code>
		</td>
		<td align="left">
		  	<code>REGULAR</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
	<tr>
		<td align="left">
			<code>2</code>
		</td>
		<td align="left">
		  	<code>mark</code>
		</td>
	    <td align="left">
		  	<code>Mark</code>
		</td>
		<td align="left">
		  	<code>pass2</code>
		</td>
		<td align="left">
		  	<code>REGULAR</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
	<tr>
		<td align="left">
			<code>3</code>
		</td>
		<td align="left">
		  	<code>john</code>
		</td>
	    <td align="left">
		  	<code>John</code>
		</td>
		<td align="left">
		  	<code>pass3</code>
		</td>
		<td align="left">
		  	<code>REGULAR</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
	<tr>
		<td align="left">
			<code>4</code>
		</td>
		<td align="left">
		  	<code>guest1</code>
		</td>
	    <td align="left">
		  	<code>Guest User 1</code>
		</td>
		<td align="left">
		  	<code>pass4</code>
		</td>
		<td align="left">
		  	<code>GUEST</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
	<tr>
		<td align="left">
			<code>5</code>
		</td>
		<td align="left">
		  	<code>guest2</code>
		</td>
	    <td align="left">
		  	<code>Guest User 2</code>
		</td>
		<td align="left">
		  	<code>pass5</code>
		</td>
		<td align="left">
		  	<code>GUEST</code>
		</td>
		<td align="left">
		  	<code>2016-01-01</code>
		</td>
	</tr>
</table>


- The data set of the previous set is used to [populate](DBSetup.html#Insert) the database table.
  
    // Populate database using the built data set
    populate(theInitialData);

- The final step is to disable auto-commit for the JDBC connection,
since we will make use of [JDBDT save-points](DBSetup.html#SaveAndRestore), 
discussed below. 

    // Set auto-commit off (to allow for save-points)
    theDB.getConnection().setAutoCommit(false);

### Database teardown
<a name="TheTestCode.DBTeardown"></a>

After all tests execute, the `globalTeardown` method is executed, 
since it is marked with the JUnit `@AfterClass` annotation. 
Its purpose is to leave the test database in a clean state and freeing up
resources.

    @AfterClass 
    public static void globalTeardown() {
      truncate(theTable);
      teardown(theDB, true);
    }

The `truncate(theTable)` statement [truncates](DBSetup.html#Clean) the `USERS` table.
Then `teardown(theDB, true)` frees up any internal resources used by the [database handle](DB.html) and closes the database connection.

### Per-test setup and teardown
<a name="TheTestCode.PerTestSetupAndTeardown"></a>

In `UserDAOTest` the `saveState` and `restoreState` methods are executed respectively before and
after each test; observe the `@Before` and `@After` JUnit annotations in each method below.
Their purpose is to make sure each test starts with the same initial database setup
([described earlier](Tutorial.html#TheTestCode.DBSetup)), 
making use of [JDBDT save-points](DBSetup.html#SaveAndRestore).

    @Before
    public void saveState() {
      // Set save point
      save(theDB);
    }
  
    @After
    public void restoreState() {
      // Restore state to save point
      restore(theDB);
    }

The `save(theDB)` method creates a database save-point, i.e., it begins
a database transaction. In symmetry, `restore(theDB)` rolls back any database
changes made by the current transaction to the JDBDT save point. 

This organization relies on disabling auto-commit for the database, 
as [described](Tutorial.html#TheTestCode.DBSetup) for `globalSetup`. 
Note also that, for portability reasons, only one save-point is maintained per database handle and 
that there must be exactly one call to `restore` per each call to `save`.

### Tests and assertions

 


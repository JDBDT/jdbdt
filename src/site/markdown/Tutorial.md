# Tutorial

This tutorial will help you understand the essential features of JDBDT.

&nbsp; <a name="TheCode"></a>
## Tutorial code 


### GitHub repository <a name="TheCode.GetIt"></a>

Get the tutorial code from [GitHub](http://github.com/edrdo/jdbdt-tutorial):
	
	git clone git@github.com:edrdo/jdbdt-tutorial.git

&nbsp; <a name="TheCode.MavenProject"></a>
### Maven project overview 

The code is organized as a [Maven](http://maven.apache.org) project, and comprises the following artifacts:

- An SQL table creation script for a table called `USERS`
(`src/main/resources/tableCreation.sql`).	
- `User`, a POJO class to store user data (`src/main/java/org/jdbdt/tutorial/User.java`)
- `UserDAO`,  a data-access object (DAO) class for user data (`src/main/java/org/jdbdt/tutorial/UserDAO.java`).
- `UserDAOTest`, a class containing [JUnit](http://junit.org) tests for `UserDAO`, making use of JDBDT (`src/test/java/org/jdbdt/tutorial/UserDAOTest.java`).
This class will be our main point of interest.
- Subclasses of `UserDAOTest`, that merely configure the database driver to use.
There are three such classes `DerbyTest`, `H2Test`, `HSQLDBTest` (in `src/test/java/org/jdbdt/tutorial`). As their name indicates, they make use of JDBC drivers for [Apache Derby](http://db.apache.org/derby), [H2](http://h2database.com), and [HSQLDB](http://hsqldb.org). 
- A JUnit test suite, `AllTests`, allowing tests in all classes mentioned above to be executed at once (`src/test/java/org/jdbdt/tutorial/AllTests.java`).

&nbsp; <a name="TheCode.RunningTheTests"></a>
### Running the tests 

In the command line go to the root folder of the project and type `mvn test` to execute the `AllTests` suite.  

Otherwise, import the project using a Maven-compatible IDE and run the tests from the IDE environment.
[Eclipse](http://eclipse.org) users will find that a `.project` file is already in the root folder.

&nbsp; <a name="TheCode.TheTestSubject"></a>
### The test subject 

The SUT of the tutorial is the `UserDAO` class. Objects of this kind 
works as a data-access object for a database table called `USERS`,
whose Java representation is given by the POJO `User` class. 
These items are described below.

#### The `USERS` table 

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
	
#### The `User` class 

The `User` class is a POJO class with getter and setter methods for each of the user attributes (e.g.,`getId` and `setId`). Additionally, it overrides a number of `java.lang.Object` methods for convenience of use in test code (e.g., `equals`). 

#### The `UserDAO` class 

The `UserDAO` class defines methods for interfacing with the `USERS` table 
using `User` objects. The methods are in correspondence to database operations
for user insertion, update, removal and retrieval.

* `insertUser(u)`: inserts a new user.
* `updateUser(u)`: update an existing user.
* `deleteUser(u)`: delete a user.
* `deleteAllUsers()`: delete all users.
* `getUser(id)`: get user data by id.
* `getUser(login)`: get user data by login.
* `getAllUsers()`: get a list of all users.
* `getUsers(r)`: get a list of all users with a given role.

&nbsp; <a name="TheTestCode"></a>
## Test code / use of JDBDT 

&nbsp; <a name="TheTestCode.Imports"></a>
### JDBDT import statements 

The test code of `UserDAOTest` makes use of JDBDT to setup and validate the
contents of the database. You should notice the following JDBDT imports:

	import static org.jdbdt.JDBDT.*; 
	import org.jdbdt.Conversion;
	import org.jdbdt.DB;
	import org.jdbdt.DataSet;
	import org.jdbdt.Table;

The static import (the very first one) relates to methods in the [JDBDT facade](Facade.html) that exposes the core JDBDT API.

&nbsp; <a name="TheTestCode.SetupAndTeardown"></a>
### Database setup and tear-down 


#### Initial setup 

To setup the database connection and define the initial contents of the database,
each subclass of `UserDAOTest` defines a `globalSetup`
method that is executed once before all tests, since it is marked with the `@BeforeClass` JUnit annotation; the method calls `UserDAO.globalSetup(dbDriverClass,dbURL)` in the parent class, parameterizing the JDBC driver class to load and the database URL to use for the actual setup. For instance, `DerbyTest` contains:

	private static final String 
    	JDBC_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String 
    	DATABASE_URL = "jdbc:derby:./db/derby/jdbdtTutorial;create=true";
    	
    @BeforeClass
    public static void globalSetup() throws Throwable {
      globalSetup(JDBC_DRIVER_CLASS, DATABASE_URL);
    }
 
This layout is merely a convenient one for the purpose of testing multiple JDBC drivers in the tutorial code. In the core code at `UserDAOTest` we have:
    
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
    	theTable = table("USERS")
                  .columns("ID",
                           "LOGIN", 
                           "NAME", 
                           "PASSWORD",
                           "ROLE",
                           "CREATED" )
                  .build(theDB);
                       
- ... plus, finally, the [data set](DataSets.html) for the initial contents of the database. The strategy in this case is to use a [data set builder](DataSets.html#Creation.Builder). 
We populate the database with 1 `ADMIN` user, 3 `REGULAR` users, and 2 `GUEST`
users. The data set builder methods allow a succinct definition of the data, as follows:

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
		// dump(theInitialData, System.err);

Uncomment the last statement above, the call to `dump`, if you wish to see some [debug output](Logs.html) sent to `System.err` listing the data set. 
The following table summarizes the created entries (note that `FIXED_DATE` equals `2016-01-01`):

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


- The data set of the previous step, `theInitialData`, is used to [populate](DBSetup.html#Insert) the database table.
  
		// Populate database using the built data set
    	populate(theInitialData);

- The final step disables auto-commit for the JDBC connection,
a prerequisite for using JDBDT save-points, that are discussed [later](Tutorial.html#TheTestCode.PerTestSetupAndTeardown) in this tutorial. 

		// Set auto-commit off (to allow for save-points)
    	theDB.getConnection().setAutoCommit(false);


#### Test teardown 

The `globalTeardown` method of `UserDAOTest`, annotated with JUnit's `@AfterClass` annotation, is executed after all tests are done.  Its purpose is to leave the test database in a clean state and freeing up any resources.

    @AfterClass 
    public static void globalTeardown() {
      truncate(theTable);
      teardown(theDB, true);
    }

The `truncate(theTable)` statement [truncates](DBSetup.html#Clean) the `USERS` table.
Then `teardown(theDB, true)` frees up any internal resources used by the [database handle](DB.html) and closes the underlying database connection.


#### Per-test setup and tear-down 


In `UserDAOTest`, the `saveState` and `restoreState` methods are executed respectively before and after each test, in line with the `@Before` and `@After` JUnit annotations in each method below. Their purpose is to make sure each test starts with the same initial database state
([described earlier](Tutorial.html#TheTestCode.SetupAndTeardown.Initial)), 
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

The `save(theDB)` call creates a database save-point, beginning
a new database transaction. In symmetry, the `restore(theDB)` call rolls back any database
changes made by the current transaction to the JDBDT save-point. 
Note also that, for portability reasons, only one save-point is maintained per database handle and that there must be exactly one call to `restore` per each call to `save`.

This setup relies on disabling auto-commit for the database in `globalSetup` 
as [described before](Tutorial.html#TheTestCode.DBSetup),
and also that `UserDAO` does not issue a database commit 
(that would make any changes permanent and terminate the transaction started with `save(theDB)`). 

&nbsp; <a name="TheTestCode.DBValidation"></a>
### Tests and assertions 

The tests in `UserDAOTest`, marked with the JUnit `@Test` annotation, validate the different methods
in `UserDAO`, using [JDBDT assertions](DBAssertions.html).
These take form as [delta assertions](DBAssertions.html#DeltaAssertions), [state assertions](DBAssertions.html#StateAssertions), or [plain data set assertions](DBAssertions.html#DataSetAssertions).

Before discussing test methods and assertions, we first make note of an auxiliary method in `UserDAOTest` called `toDataSet`, that is used throughout the rest of the code. It provides a shorthand to create  [a (typed) data set](DataSets.html#Creation.Typed) from a single `User` instance. The conversion from `User` instances to row format is defined by the `CONVERSION` function (defined as a lambda expression):

    private static final Conversion<User> CONVERSION = 
      u -> new Object[] { 
        u.getId(), 
        u.getLogin(), 
        u.getName(), 
        u.getPassword(),
        u.getRole().toString(),
        u.getCreated()
      };
      
    static DataSet toDataSet(User u) {
      return data(theTable, CONVERSION).row(u);
    }

#### Delta assertions <a name="TheTestCode.DBValidation.DeltaAssertions"></a>

As an example of a [delta assertion](DBAssertions.html#DeltaAssertions), consider `testNonExistingUserInsertion`:

    @Test
    public void testNonExistingUserInsertion() throws SQLException {
      User u = nonExistingUser();
      theDAO.insertUser(u);
      assertInserted("DB change", toDataSet(u));
    }
    
The code tests whether a new user is correctly inserted in the database via `UserDAO.insertUser`. 
It proceeds by first calling `nonExistingUser()`, an auxiliary method to creates a `User` instance that does not correspond to any entry in the `USERS` table. 
Then it calls `theDAO.insertUser(u)` to insert the user. 
To validate the database change `assertInserted`, a [delta assertion](DBAssertions.html#DeltaAssertion) method, is used. The assertion specifies that the expected state should differ only by the addition of the new user, i.e., `toDataSet(u)`. A fresh database query is issued for the `USERS` table, and the delta is verified against the [database snapshot](DBAssertions.html#Snapshots) defined in the [initial setup](Tutorial.html#TheTestCode.DBSetup) of `globalSetup`, more precisely the `populate(theInitialData)` step in that method. 

#### State assertions 

Now consider `testNonExistingUserInsertionVariant`, an alternative test method with the same purpose as `testNonExistingUserInsertion`, but that uses a [state assertion](DBAssertions.html#StateAssertions) instead of a delta assertion:
	
    @Test
    public void testNonExistingUserInsertionVariant() throws SQLException {
      User u = nonExistingUser();
      theDAO.insertUser(u);
      DataSet expected = DataSet.join(theInitialData, toDataSet(u));
      assertState("DB state", expected);
  }

The assertion method is `assertState`, that takes the data set that is expected
to match the current database state. The expected data set is formed by
`theInitialData`, the data set defined in `globalSetup`, joined with `toDataSet(u)`.


#### Plain data set assertions 

[Plain data set assertions](DBAssertions.html#DataSetAssertions) match the contents of two data set instances, via the `assertEquals` method
(this should not be confused with the JUnit assertion method variants with the same name).  
For instance, the method is used in `testGetAllUsers`:

    @Test
    public void testGetAllUsers() throws SQLException {
      List<User> list = theDAO.getAllUsers();
      DataSet expected = theInitialData;
      DataSet actual = data(theTable, CONVERSION).rows(list);
      assertEquals("User list", expected, actual);
      assertUnchanged("No DB changes", theTable); 
    }
    
*Note*: in addition to verifying the result of `getAllUsers` through `assertEquals`, 
the test code above also validates that `getAllUsers` did not change
the `USERS` table through the call to `assertUnchanged` (a delta assertion method). 
This assertion provides an extra guarantee on the functionality of `getAllUsers`. 

#### Inspecting assertion errors

When an assertion fails, `DBAssertionError` is thrown by JDBDT. 
Additionally, error information may be [logged](DB.html#Logging) to a file or output stream in [an XML format](Logs.html). By default, assertion errors will be logged to `System.err`. Consider for instance `testExistingUserDelete` in `UserDAOTest`: 

    @Test
    public void testExistingUserDelete() throws SQLException {
      User u = anExistingUser(); // -> change to nonExistingUser()
      boolean deleted = theDAO.deleteUser(u);
      assertDeleted("DB change", toDataSet(u));
      assertTrue("return value", deleted);
    }

If you change `anExistingUser()` above to `nonExistingUser()`, 
then `assertDeleted`, two lines below, will throw `DBAssertionError`.
The user instance returned by `nonExistingUser()` does not exist in the database,
hence `theDAO.deleteUser(u)` will fail to delete the equivalent entry
in the `USERS` table.

In conjunction with `DBAssertionError`, the log message below will appear in `System.err`, where `99` / `john99` refers to the non-existing user. 
The assertion error is explained
by the `jdbdt-log-message/delta-assertion/errors/old-data` section, indicating that 
the (non-existing) user entry was expected to be deleted but was actually not. 
For more details on the logging format, refer to [this page](Logs.html).

    <jdbdt-log-message ...>
      ...
      <delta-assertion>
        ...
        <errors>
          <old-data>
            <expected count="1">
              <row>
                <column java-type="java.lang.Integer" label="ID">99</column>
                <column java-type="java.lang.String" label="LOGIN">john99</column>
                <column java-type="java.lang.String" label="NAME">John Doe 99</column>
                <column java-type="java.lang.String" label="PASSWORD">doeit 99</column>
                <column java-type="java.lang.String" label="ROLE">REGULAR</column>
                <column java-type="java.sql.Date" label="CREATED">2016-01-01</column>
              </row>
            </expected>
            <actual count="0"/>
          </old-data>
          <new-data>
            <expected count="0"/>
            <actual count="0"/>
          </new-data>
        </errors>
      </delta-assertion>
    </jdbdt-log-message>




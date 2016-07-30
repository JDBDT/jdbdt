# Tutorial

This tutorial helps you understand how to use the essential
features of JDBDT.  It assumes that you are reasonably familiar with [Maven](http://maven.org) and [JUnit](http://junit.org), since that the tutorial code is organized as a Maven project (you can download), and that JUnit tests are used as illustration.

**Contents**

* 	[Tutorial code](Tutorial.html#TheCode)
	*	[Getting the code](Tutorial.html#TheCode_GetIt)
	*	[Maven project overview](Tutorial.html#TheCode_MavenProject)
	*   [Running the tests](Tutorial.html#TheCode_RunningTheTests)
* 	[Test subject](Tutorial.html#TheTestSubject)
	*	[The USERS table](Tutorial.html#TheTestSubject_Table)
	*	[The User class](Tutorial.html#TheTestSubject_UserClass)
	*	[The UserDAO class](Tutorial.html#TheTestSubject_UserDAOClass)
* 	[Test code](Tutorial.html#TheTestCode)

## Tutorial code
<a name="#TheCode"></a>

### Getting the code
<a name="TheCode_GetIt"></a>

You may clone the tutorial code from [the github repository](http://github.com/edrdo/jdbdt-tutorial):
	
	git clone git@github.com:edrdo/jdbdt-tutorial.git

### Maven project overview
<a name="TheCode_MavenProject"></a>

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
<a name="TheCode_RunningTheTests"></a>

Run `mvn test` from the command line in the root folder to execute the `AllTests` suite.  Otherwise, import the project using a Maven-compatible IDE.  
[Eclipse](http://eclipse.org) users will find that a `.project` file is already in the root folder ([M2Eclipse plugin required](http://www.eclipse.org/m2e/)).

## The test subject
<a name="TheTestSubject"></a>

The SUT of the tutorial is a `UserDAO` class. Objects of this kind 
works as a data-access object for a database table called `USERS`,
whose Java representation is given by the POJO `User` class. 
These items are described below.

### The `USERS` table 
<a name="TheTestSubject_Table"></a>

The `USERS` table represents user data in the form of a numeric id (primary key), a unique login, a name, a password, a role, and a creation date. The code for table creation below should be self-explanatory.  A sequence or identity column setting could be associated to the `ID` column, but we keep the example as simple as possible to ensure portability for different database engines. Likewise, for `ROLE`, a reference table or an `ENUM` type (as supported by some engines) could be used alternatively.

	CREATE TABLE USERS 
	(
		ID INTEGER PRIMARY KEY NOT NULL ,
		LOGIN VARCHAR(16) UNIQUE NOT NULL,
		NAME VARCHAR(32) NOT NULL,
		PASSWORD VARCHAR(32) NOT NULL,
		ROLE VARCHAR(7) DEFAULT 'REGULAR' NOT NULL
		  CHECK (ROLE IN ('ADMIN', 'REGULAR', 'GUEST')),
		CREATED DATE
	)
	
### The `User` class
<a name="TheTestSubject_UserClass"></a>

The `User` class is a POJO class with getter and setter methods for each of the user attributes (e.g., `getId()`, `setId`). Additionally, it overrides a number of `java.lang.Object` methods for convenience of use in test code (e.g., `equals`). 

### The `UserDAO` class

<a name="TheTestSubject_UserDAOClass"></a>

The `UserDAO` class defines methods for interfacing with the `USERS` table 
using `User` objects. The methods are correspondence to database operations
for user insertion, update, removal and retrieval:

* `insertUser(u)`: inserts a new user;
* `updateUser(u)`: update an existing user;
* `deleteUser(u)`: delete a user;
* `deleteAllUsers()`: delete all users;
* `getUser(id)`: get user data by id;
* `getUser(login)`: get user data by login; 
* `getAllUsers()`: get a list of all users;
* `getUsers(r)`: get a list of all users with role `r`;

## Test code

### Database setup and teardown

### Test setup and teardown using save-points

### Tests and assertions

 


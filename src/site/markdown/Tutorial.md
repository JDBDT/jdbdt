# Tutorial

This tutorial will help you understand how to use the essential
features of JDBDT. 


## Tutorial code

To get started you may clone the tutorial code from github
	
	git clone git@github.com:edrdo/jdbdt-tutorial.git
	
The code is organized as a Maven project. It comprises the following 4 artifacts:

1. A simple database defined a single table called `USERS`
(`src/main/resources/tableCreation.sql`).	
2. `User`, a POJO class to store user data (`src/main/java/org/jdbdt/tutorial/User.java`)
3. `UserDAO`,  a data-access object (DAO) class for user data (`src/main/java/org/jdbdt/tutorial/UserDAO.java`);
4. `UserDAOTest`, a JUnit class containing tests for `UserDAO`, making use of JDBDT (`src/test/java/org/jdbdt/tutorial/UserDAOTest.java`).
This class will be our point of interest.

## Test subject

Throughout this tutorial we will mostly focus on `UserDAOTest`, the last item (item 4 above), since this is where the JDBDT functionality is used. But first let us provide
a glimpse of the remaining ones (1-3 above).

### `USERS` table 

The `USERS` table represents user data in the form of a numeric id, a login, a name,
a password, a role, and a creation date. The code for table creation below should be 
self-explanatory.  A sequence or identity column setting could be associated to the `ID` column, but 
we keep the example as simple as possible to ensure the script runs for different database engines.
Also, a reference table (for a better design style) for roles or an enum type (as supported by some engines) 
could be used in association to the `ROLE` field.

	CREATE TABLE USERS 
	(
		ID INTEGER PRIMARY KEY NOT NULL ,
		LOGIN VARCHAR(16) UNIQUE NOT NULL,
		NAME VARCHAR(32) NOT NULL,
		PASSWORD VARCHAR(32) NOT NULL,
		ROLE VARCHAR(7) DEFAULT 'REGULAR' NOT NULL
		CHECK (ROLE IN ('ADMIN', 'REGULAR', 'GUEST')),
		CREATED DATE
	);
	
### `User`  

The `User` class defines getter and setter methods (e.g., `getId()`, `setId`) for each of the user attributes. Plus, it overrides a number of `java.lang.Object` methods for convenience of use in test code (e.g., `equals`). 

### `UserDAO` 

The `UserDAO` class defines methods for interfacing with the `USERS` table 
using `User` instances. The methods are correspondence to elementary database operations
for user insertion, update, removal and retrieval:

* `insertUser(u)`: inserts a new user;
* `updateUser(u)`: update an existing user;
* `deleteUser(u)`: delete a user;
* `deleteAllUsers()`: delete all users;
* `getUser(id)`: get user data by id;
* `getUser(login)`: get user data by login; 
* `getAllUsers()`: get a list of all users;
* `getUsers(r)`: get a list of all users with role `r`;

## Test class

TODO


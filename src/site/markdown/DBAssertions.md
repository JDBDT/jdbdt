
# Assertions

There are three kinds of JDBDT assertions:

1. **Delta (&delta;) assertions** verify database state against user-specified
incremental changes (a **database delta**).
2. More traditional **state assertions** verify that the database contents match a given data set. 
3. Finally, **data set assertions** let you verify if two data sets are equivalent.

## Delta assertions

&delta;-assertions state the expected incremental changes made to the database,
i.e., an expected database delta. The figure below illustrates the mechanism. 
Starting from state **S**, called the **reference snapshot**, the SUT (software under test)
acts on the database, yielding a new database state, **S'**. **S** and **S'** 
will have in common **unchanged** data **U = S &cap; S'**,
but will differ by **&delta; = (O, N)**, where **O = S &minus; S'** is the **old** data in **S** no longer defined in **S'**, and **N = S' &minus; S** is the **new** data in **S'**.

![Database delta](images/jdbdt-delta.png)

The programming pattern in line with this scheme is as follows:

	Define reference snapshot(s) for the data source(s) of interest
	theSUT.changesTheDB();
	Call delta assertion method(s)
	
### Snapshots 
<a name="Snapshots"></a>

A data source **snapshot** is a data set that is used as reference for subsequent delta
assertions. It can be defined in two ways for a data source `s`:

1.  A call to `populate(data)`, s.t. `data.getSource() == s` and `s` is a `Table` instance 
will set `data` as the snapshot for `s`. Since `populate(data)` resets the full table
contents exactly to `data`, by definition it will be safe to assume it as the correct database state.
2. A call to `takeSnaphot(source)`, regardless of the type of `s` (`Table`, `Query`)
will issue a fresh database query, and record the obtained data set as the snapshot for `s`.

*Illustration*

	import static org.jdbdt.JDBDT.*;
	import org.jdbdt.DataSet;
	import org.jdbdt.Table;
	import org.jdbdt.DataSource;
	...
	// [1] Populate a table with some data.
	Table t = ...;
	DataSet data = data(t). ...;
	populate(data); // --> 'data' becomes the reference snapshot
	
	// [2] OR take a snapshot.
	DataSource s = ... ; // 's' can be a Table or Query
	takeSnapshot(s); // --> internally takes and records a snapshot 
	
### Assertion methods 

The elementary &delta;-assertion method is `assertDelta`. 
An `assertDelta(oldData, newData)` call,
where `oldData` and `newData` are data sets for the same data source `s`,
checks if the database delta is `(oldData,newData)`, as follows:

1. It issues a new database query for `s`.
2. It computes the actual delta between the query's result and the reference snapshot.
3. It verifies if the expected and actual deltas match. If they do not match, `DBAssertionError`
is thrown, and details on mismatched data are logged (unless `DB.Option.LogAssertionErrors` is disabled). 

A number of other assertion methods are defined for convenience, all of which internally reduce 
to `assertDelta`, as follows:

<table border="1">
	<tr>
		<th align="left">Method</th>
		<th align="left">Description</th>
		<th align="center">O</th>
		<th align="center">N</th>
	</tr>
	<tr>
		<td><code>assertDelta([msg,] oldData, newData)</code></td>
	    <td>Asserts that <code>&delta; = (oldData,newData)</code>.</td>
	    <td align="center"><code>oldData</code></td>
	    <td align="center"><code>newData</code></td>
	</tr>
    <tr>
		<td><code>assertDeleted([msg,] data)</code></td>
	    <td>Asserts that <code>data</code> was deleted.</td>
	    <td align="center"><code>data</code></td>
	    <td align="center"><code>&empty;</code></td>
	</tr>
	<tr>
		<td><code>assertInserted([msg, ] data)</code></td>
	    <td>Asserts that <code>data</code> was inserted.</td>
	    <td align="center"><code>&empty;</code></td>
	    <td align="center"><code>data</code></td>
	</tr>
	<tr>
		<td><code>assertUnchanged([msg,] source)</code></td>
	    <td>Asserts no database changes took place.</td>
	    <td align="center"><code>&empty;</code></td>
	    <td align="center"><code>&empty;</code></td>
	</tr>
</table>

*Illustration*

	DB db = ... ;
	Table t = table(db, "USER")
	          .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	...        
	// Assert an insertion
	... define snapshot with populate or takeSnapshot ...
	letTheSUT_insertOneUser( ... ); 
	assertInserted(data(t).row(999, "john", "John Doe", "justDoeIt", Date.valueOf("2016-01-01"));
	
	// Assert a removal
    ... define snapshot with populate or takeSnapshot ...
    letTheSUT_removeUserById( 999 ); 
    assertDeleted(data(t).row(999, "john", "John Doe", "justDoeIt", Date.valueOf("2016-01-01"));
	
	// Assert an update.
	... define snapshot with populate or takeSnapshot ...
	DataSet before = data(t).row(999, "john", "John Doe", "justDoeIt", Date.valueOf("2016-01-01"));
    DataSet after  = data(t).row(999, "john", "John Doe", "dontDoeIt", Date.valueOf("2016-01-01"));
	letTheSUT_updatePassword(999, "dontDoeIt")
	assertDelta(before, after);
	
	// Assert that no changes took place.
	... define snapshot with populate or takeSnapshot ...
	letTheSUT_doNoChangesToDB();
	assertUnchanged(t);
	
	
## State assertions

A state assertion checks that the database contents in full, and
is executed by calling `assertState`. 
An `assertState([msg,] data)` call verifies that the data stored in
the database for `data.getSource()` is (exactly) `data` as follows:

1. It issues a new database query for `data.getSource()`.
2. It verifies if the obtained data matches the expected `data`. If they do not match, `DBAssertionError`
is thrown, and details on mismatched data are logged (unless `DB.Option.LogAssertionErrors` is disabled). 

Note that no reference snapshot needs to be set for a state assertion, unlike &delta;-assertions.

An `assertEmpty([msg,], source)` call is equivalent to `assertState([msg,], empty(source))`, i.e.,
it verifies that the given data source has no rows.

*Illustration*

	DB db = ... ;
	Table t = table(db, "USER")
	          .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	...        
	// Assert that table is empty.
	letTheSUTDeleteAllUsers( ... );
	assertEmpty(t);
	
	// Assert state after insertion
	DataSet initialData = ...;
	populate(initialData);
	...
	DataSet expected = 
	  DataSet.join(initialData, 
	               data(t).row(999, "john", "John Doe", "justDoeIt", Date.valueOf("2016-01-01"))); 
	letTheSUT_insertOneUser( ... ); 
	assertState(expected);
	
## Data set assertions
	
A data set assertion verifies that two data sets are equivalent.
It is performed using the `assertEquals` method.

*Illustration*

    // Fill a database table.
	DB db = ... ;
	Table t = table(db, "USER")
	          .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	DataSet initialData = ...; 
	populate(initialData);
	...      
	// Assert that all data is returned.
	DataSet expected = initialData; 
	List<User> listOfUsers = letTheSUTQueryAllUsers( ... );
	Conversion<User> conv = ...;
	DataSet actual = data(t, conv).rows(listOfUsers);
	assertEquals(expected, actual);
	

**Note for JUnit users**: beware not to confuse `assertEquals` with [JUnit](http://junit.org) methods that go by the same name. JUnit's `assertEquals` will not work properly, since `DataSet` (deliberately) does *not* override `Object.equals`. All will go well if you use [a static import for all methods in the JDBDT facade](Facade.html#StaticImport).


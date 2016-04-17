
# Data sets

A `DataSet` object represents a collection of rows for a [data source](DataSources.html)
that may be used for database [setup](DBSetup.html) or [verification](DBAssertions.html).

## Creation
<a name="Creation"></a>

The examples below define data sets for a [table](DataSources.html#Table) (`Table`) object, 
but the definition of data sets is similar for other data sources like 
[queries](DataSources.html#Query) (`Query`) or [custom SQL data sources](DataSources.html#SQLDataSource) (`SQLDataSource`).

### Plain definition 
<a name="Creation.Plain"></a>

In the simplest manner, 
`DataSet` objects are created through the `data` JDBDT facade method,
typically followed by a chained sequence of calls.

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DataSet;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
	DB db = ...;
	Table table = db.table("Users")
	                .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	DataSet userSet 
       = data(t)
		.row(0,   "root",  "Root User",  "god",       null)
	    .row(101, "john",  "John Doe",   "justDoeIt", Date.valueOf("2014-07-12")
	    .row(102, "harry", "Harry H",    "meta",      Date.valueOf("2016-01-01")
	    .row(103, "guest", "Guest User",  "welcome",  Date.valueOf("2016-01-02");

### Typed data sets
<a name="Creation.Typed"></a>

`TypedDataSet` is a typed extension of `DataSet`. It allows for a simple
form of (one-way) object-relational mapping through conversion functions expressed
by the `Conversion` interface. A `Conversion` instance 
defines a mapping from objects to rows, where each row is expressed as an array 
of column values.

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.TypedDataSet;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
	DB db = ...;
	Table table = db.table("Users")
	                .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
     
	Conversion<User> conv = u -> new Object[] {  
	                          u.getID(), 
	                          u.getLogin(),
	                          u.getPassword(),
	                          u.getCreationDate() 
	                        };
    User root = ..., john = ..., harry = ..., guest = ...;
	List<User> listOfOtherUsers ...;  
	                 
	TypedDataSet<User> userSet  
	   = data(t, conv)
		.row(root)
		.rows(john, kafka, guest)
		.rows(listOfOtherUsers);

### Data set builders
<a name="Creation.Builder"></a>

A `DataSetBuilder` instance can be used to define or augment a data set 
with the aid of expressive column filler methods. For instance,
there are column fillers for value sequences or pseudo-random values.
Many of the column fillers may be defined concisely, for example
making use of lambda expressions, arrays, or collections.

The `builder` facade method creates a builder for a fresh data set and
the `DataSet.build` method lets you add rows to a previously defined data
set.
    
    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    ...
	DB db = ...;
	DataSet data = builder(
	
	DB db = ...;
	Table table = db.table("Users")
	                .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");	
    
    // Create a fresh data set with 9 rows
    DataSet data = 
       builder(t)
      .sequence("ID", 1) // 1, 2, 3, ...
      .sequence("LOGIN", "harry", "mark", "john")
      .sequence("NAME", "Harry H", "Mark M", "John J")
      .sequence("PASSWORD", i -> "password " + i , 1)
      .random("CREATED", Date.valueOf("2015-01-01"), Date.valueOf("2015-12-31"))
      .generate(3) // generate 3 rows, keep with ID sequence and CREATED random filler
      .sequence("LOGIN", i -> "guest_" + i, 4)  // "user_4", "user_5", ...
      .sequence("NAME", i -> "Guest User " + i, 4) // "Guest User 4", ...
      .value("password", "samePasswordForAllGuests") 
      .generate(6)  
      .data();   
      
    // Add 500 more rows to the data set
    data.build()
      .sequence("ID", 1000) // 1000, 1001, ... 
      .sequence("LOGIN", i -> "anotherUser" + i, 1000)
      .sequence("NAME", i -> "Yet Another User" + i, 1000)
      .random("PASSWORD", "aeiou", "qwerty", "12345", "pass is the password")
      .nullValue("CREATED") // set to NULL
      .generate(500);
 
## Read-only data sets
<a name="ReadOnly"></a>

Data sets are marked as read-only when they are set as [database snapshots](DBAssertions.html#Snapshots). After a data set is marked read-only, attempts
to modify it will result in `org.jdbdt.InvalidOperationException` being thrown.

## Quick method reference
<a name="MethodReference"></a>

### Facade methods in `JDBDT`

For a [data source](DataSources.html) `s`:

- `data(s)` creates a new data set.
- `data(s,c)` creates a new typed data set with conversion function `c`.
- `builder(s)` creates a data set builder.

### Instance methods 

- `row` and `rows` add rows to the data set (see examples above).
- `build` creates a new data set builder backed up by the data set (see example above).
- `size()` returns the number of rows.
- `isEmpty()` indicates if a data set is empty.
- `add(other)` adds all rows from `other` to the data set.
- `isReadOnly()` indicates if `data is read-only.
- `setReadOnly()` sets `data` as read-only.

### Static utility methods 

- `subset(data, index, n)` returns a new data set containing `n` rows of `data` starting from the `index`-th row.
- `singleton(data,i)` returns a new data set containing only the `index`-th row in `data`.
- `head(data, n)` returns a new data set containing the first `n` rows of `data`.
- `tail(data,n)` returns a new data set containing the last `n` rows of `data`.




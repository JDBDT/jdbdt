
# Data sets

A `DataSet` object represents a collection of rows for a [data source](DataSources.html)
that may be used for database [setup](DBSetup.html) or [assertions](DBAssertions.html).

<a name="Creation"></a>

## Creation


The examples below define data sets for a [table](DataSources.html#Table) (`Table`) object, 
but the definition of data sets works similarly for
[queries](DataSources.html#Query) (`Query`).

<a name="Creation.Plain"></a>

### Plain definition 

In the simplest manner, 
`DataSet` objects are created through the `data` JDBDT facade method,
typically followed by a chained sequence of calls.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DataSet;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
	DB db = ...;
	Table t = table("USERS")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	         .build(db);
	         
	DataSet users
       = data(t)
		.row(0,   "root",  "Root User",  "god",       null)
	    .row(101, "john",  "John Doe",   "justDoeIt", Date.valueOf("2014-07-12"))
	    .row(102, "harry", "Harry H",    "meta",      Date.valueOf("2016-01-01"))
	    .row(103, "guest", "Guest User", "welcome",   Date.valueOf("2016-01-02"));


<a name="Creation.Typed"></a>

### Typed data sets

`TypedDataSet` is a typed extension of `DataSet`. It allows for a simple
form of (one-way) object-relational mapping through conversion functions expressed
by the `Conversion` interface. A `Conversion` instance 
defines a mapping from objects to rows, where each row is expressed as an array 
of column values.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.TypedDataSet;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
	DB db = ...;
	Table t = table("USERS")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	         .build(db);
     
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
		.rows(john, harry, guest)
		.rows(listOfOtherUsers);


<a name="Creation.Builder"></a>

### Data set builders

A `DataSetBuilder` instance can be used to define or augment a data set 
with the aid of expressive column filler methods. For instance,
there are column fillers for value sequences or pseudo-random values.
Many of the column fillers may be defined concisely, for example
making use of lambda expressions, arrays, or collections.

The `builder` facade method creates a builder for a fresh data set and
the `DataSet.build` method lets you add rows to a previously defined data
set.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    ...
	DB db = ...;
	Table t = table("USERS")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	         .build(db);	
    
    // Create a fresh data set with 9 rows
    DataSet data = 
       builder(t)
      .sequence("ID", 1) // 1, 2, 3, ...
      .sequence("LOGIN", "harry", "mark", "john")
      .sequence("NAME", "Harry H", "Mark M", "John J")
      .sequence("PASSWORD", i -> "password " + i , 1)
      .random("CREATED", Date.valueOf("2015-01-01"), Date.valueOf("2015-12-31"))
      .generate(3) // generate 3 rows, 
      .sequence("LOGIN", i -> "guest_" + i, 4)  // "user_4", "user_5", ...
      .sequence("NAME", i -> "Guest User " + i, 4) // "Guest User 4", ...
      .value("password", "samePasswordForAllGuests") 
      .generate(6) // 6 more rows keeping ID sequence and CREATED random filler
      .data();   
      
    // Add 500 more rows to the data set
    data.build()
        .sequence("ID", 1000) // 1000, 1001, ... 
        .sequence("LOGIN", i -> "anotherUser" + i, 1000)
        .sequence("NAME", i -> "Yet Another User" + i, 1000)
        .random("PASSWORD", "aeiou", "qwerty", "12345", "pass is the password")
        .nullValue("CREATED") // set to NULL
        .generate(500);
 

<a name="ReadOnly"></a>

## Read-only data sets

A data set is marked read-only when defined as a [database snapshot](DBAssertions.html#Snapshots).
Any attempt to modify it subsequently will cause an `InvalidOperationException`.




<a name="CSV"></a>
## Importing / exporting data sets from/to CSV format

Data sets can be imported / exported from/to CSV format. 
The format supported complies with [RFC-4180](https://www.ietf.org/rfc/rfc4180.txt) except for the possibility of line breaks or carriage returns within escaped sequences. 
The CSV separator (comma by default) and escape (double-quote by default) characters
are configurable as well as other aspects.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.CSV;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    import org.jdbdt.DataSet;
    ...
	DB db = ...;
	Table table = table("USERS")
	             .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	             .build(db);
	CSV.Format format = new CSV.Format()
	                   .separator('\t')
	                   .useReadConversions();
	DataSet ds = read(table, format, new File("mydata.csv")); 
	...
	write(ds, format, new File("mydata2.csv));
     
<a name="SummaryOfMethods"></a>
## Summary of API methods

### `JDBDT`

Object creation - for a [data source](DataSources.html) `s`:

- `data(s)` creates a new data set.
- `data(s, c)` creates a new typed data set with conversion function `c`.
- `builder(s)` creates a data set builder with an underlying fresh data set.
- `empty(s)` returns an empty & read-only data set that is unique for `s`. 

CSV:

- `read(s,f,inp)`: reads a data set for data source `s` with CSV format `f` from file `inp`.
- `write(ds,f,out)`: writes data set `ds` using CSV format `f` onto file `out`.

Debugging:

- `dump(s, out)` dump database contents of data source `s` to stream/file `out`. 
- `dump(data, out)` dump the contents of data set `data` to stream/file `out`.

### `DataSet`, `TypedDataSet`

Properties:

- `size()` returns the number of rows.
- `isEmpty()` indicates if the data set is empty.
- `isReadOnly()` indicates if the data set is read-only.
- `getDataSource()` returns the [data source](DataSources.html) instance for the data set.
- `toString()` returns a textual representation of the data set (see also `dump` above).

Mutators:

- `row` and `rows` methods add rows to the data set (see examples above).
- `build()` creates a new data set builder backed up by the data set (see example above).
- `add(other)` adds all rows from `other` to the data set.
- `setReadOnly()` sets the data set as read-only.

Utility methods (all `static`):

- `copyOf(data)` returns a new data set that has the same rows as `data`.
- `join(data1, data2, ...,)` returns a new data set that contains all the rows in `data1`, `data2`, ...
- `subset(data, index, n)` returns a new data set containing `n` rows of `data` starting from the `index`-th row.
- `singleton(data, i)` returns a new data set containing only the `index`-th row in `data`.
- `first(data, n)` returns a new data set containing the first `n` rows of `data`.
- `last(data, n)` returns a new data set containing the last `n` rows of `data`.

### `DataSetBuilder`

- `data()` returns the underlying data set.
- `generate(n)` adds `n` rows to the underlying data set based on the current column filler settings.
- `value(column, v)`  sets a constant filler with value `v` for `column`.
- `nullValue(column)` sets a `NULL` value filler for `column`.
- `remainingColumnsNull()` sets the `NULL` value filler for all remaining columns
- `allColumnsNull()` sets the `NULL` value filler for all columns
- `sequence(column, ...)`  defines a sequence fillers for `column` (several method variants).
- `random(column, ...)` defines a pseudo-random filler for `column` (several method variants).
- `set(column, filler)` defines a custom column filler for `column`. 

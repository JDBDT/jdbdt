
# Data sets

A data set is a collection of rows that associates to a [data source](DataSources.html). 


## Plain definition 

`Data Set` instances are created through `data` JDBDT facade.
through a chained sequence of calls.

Example:

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

## Typed data sets.

`TypedDataSet` is a typed extension of `DataSet` that allows for a simple
form of (one-way) object-relational mapping through conversion functions.
A `Conversion` instance defines a mapping between objects and row format, i.e.,
arrays of columns.

Example:

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

## Data set builders
 
`DataSetBuilder` instances can be used to define data sets 
with the aid of expressive column filler methods. 
There are column fillers for value sequences, pseudo-random values,
amongst other patterns. 


    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.TypedDataSet;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
	DB db = ...;
	DataSet data = builder(
	
     build(t)
 *    .sequence("id", 1) // 1, 2, 3, ...
 *    .sequence("login", "harry", "mark", "john")
 *    .nullValue("password")
 *    .random("since", Date.valueOf("2015-01-01"), Date.valueOf("2015-12-31"))
 *    .generate(3) // generate 3 rows
 *    .sequence("login", i -&gt; "user_" + i, 4)  // "user_4", ... , "user_7"
 *    .value("password", "dumbPass")
 *    .generate(4) // generate 4 more rows 
 *    .getData();   
 

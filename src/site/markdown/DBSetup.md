# Database setup

The contents of database tables may be defined using database setup methods 
in the `JDBDT` facade.

## Cleaning data

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



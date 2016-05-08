# Database setup

The contents of database tables may be defined using database setup methods 
in the `JDBDT` facade.

## Cleanup methods

Database data may be cleaned up as follows:

* `deleteAll(t)`: clears  the entire contents of table `t` using a DELETE statement;
* `deleteAllWhere(t, w, [,args])`: clears the contents of table `t` subject to WHERE clause `w` 
and optional WHERE clause arguments `args`, using a DELETE statement;
* `truncate(t)`: clears the entire contents of table `t` using a TRUNCATE statement;

Note that `truncate` executes a TRUNCATE TABLE statement, and may typically execute faster than `deleteAll`,
which in turn uses DELETE statement (with no WHERE clause). The TRUNCATE TABLE statement, however, 
may not respect integrity constraints and has variable semantics 
for different database engines; check some details [here](https://en.wikipedia.org/wiki/Truncate_(SQL). Additionally, note that the TRUNCATE TABLE statement is [not supported](Compatibility.html#KnownIssues) by some database engines.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table t = table(db, "USER")
	         .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");
	...
	// Clear table using TRUNCATE.
	truncate(t);
	
	// Clear table using DELETE.
	deleteAll(t);
	
	// Delete all users whose login matches a certain filter
	String loginFilter = ...;
	deleteAll(t, "LOGIN LIKE ?", loginFilter);
	  
## Database insertions

Database data may be inserted up as follows:

* `insert(data)`: inserts the rows in `data` into the table given by `data.getSource()`;
* `populate(data)` inserts rows like `insert`, but clears the table first using a DELETE statement,
and sets `data` as the [snapshot for subsequent delta assertions](DBAssert.html#Snapshots); 

Hence, `insert` should be used for incremental additions to a table, whereas
`populate` should be used to reset the contents of a table contents entirely. 
The use of `populate` is adequate in particular if &delta;-assertions are performed over the table
subsequently.

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
    
    // Reset contents of USER to data.
    populate(data); 
    ...
    
    // Insert data in USER table (does not clear previous contents).
    insert(data);
    





# Database setup

The contents of database tables may be defined using database setup methods 
in the `JDBDT` facade.

## Cleanup methods

Database data may be cleaned up as follows:

* `truncate(t)`: clears the entire contents of table `t` using a TRUNCATE statement;
* `deleteAll(t)`: clears  the entire contents of table `t` using a DELETE statement;
* `deleteAllWhere(t, w, [,args])`: clears the contents of table `t` subject to WHERE clause `w` 
and optional WHERE clause arguments `args`, using a DELETE statement;

Example:

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

* `insert(data)` inserts the rows in data set `data` to the table given by `data.getSource()`;
* `populate(data)` adds rows like `insert`, but clears the table first and sets `data` as 
the [snapshot for subsequent delta assertions](DBAssert.html#Snapshots); 

Hence, `insert` should be used for incremental additions to a table, whereas
`populate` should be used to reset a table contents. The latter in particular
is also best suited for the use of &delta;-assertions.




# Database setup

The contents of database tables may be defined using database setup methods 
in the `JDBDT` facade.

## Cleanup methods

Database data may be cleaned up as follows:

* `truncate(t)`: clears the entire contents of table `t` using a TRUNCATE statement;
* `deleteAll(t)`: clears  the entire contents of table `t` using a DELETE statement;
* `deleteAllWhere(t, w, [,args])`: clears the contents of table `t` subject to WHERE clause `w` 
and optional WHERE clause arguments `args`, using a DELETE statement;

Note that `truncate` (i.e., a TRUNCATE statement) may typically execute faster than `deleteAll` 
(a DELETE statement). However, TRUNCATE may not respect integrity constraints and has variable semantics 
across database engines; check some details [here](https://en.wikipedia.org/wiki/Truncate_(SQL). Additionally, the TRUNCATE statement is [not supported](Compatibility.html#KnownIssues) by some database engines.

*Example*

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
The use of `populate` may be adequate in particular if &delta;-assertions are performed over the table
subsequently.






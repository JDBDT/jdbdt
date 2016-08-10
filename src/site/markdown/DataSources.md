
# Data sources

`DataSource` objects represent tables and queries that are used for database 
 [setup](DBSetup.html) or [assertions](DBAssertions.html).
 
## Tables
<a name="Table"></a>

Tables are represented by `Table`, a subclass of `DataSource`, and created
using the `table` facade method in association to a [database handle](DB.html). 
The `columns` method may be used to specify the table columns of interest; all
columns will be considered by default otherwise.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table userTable = table(db, "USER")
	                 .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");

## Queries
<a name="Query"></a>

Queries are represented by `Query`, a subclass of `DataSource`. 
A `Query` object can be created from a raw SQL statements or using a `QueryBuilder`.    
             
### Definition from raw SQL 
<a name="RawQuery"></a>

The `query` facade method may be used to define a query using raw SQL.
 
 *Illustration*
        
    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Query;
    ...
    DB db = ...;
    // Query user login and name by id.
    int idArgument = ...;
    Query q = query(db, "SELECT LOGIN, NAME FROM USER WHERE ID = ?", idArgument);

### Definition using `QueryBuilder`
<a name="QueryBuilder"></a>

`QueryBuilder` objects can be used to define queries programmatically.
The `select` facade method creates a query builder that can be parameterized
using a chained sequence of calls. A final call to `QueryBuilder.build` in 
such a sequence creates a `Query` object. The parameterization methods are the following:

* `from`: defines the `FROM` clause;
* `where`: defines a `WHERE` clause; 
* `distinct`: defines a `DISTINCT` modifier for the query;
* `orderBy`: defines an `ORDER BY` clause;
* `groupBy`: defines  a `GROUP BY` clause;
* `having`: defines a `HAVING` clause.

Note to `orderBy`: [database assertions](DBAssertions.html) are insensitive 
to the order of query results, but the use of `orderBy` may make it easier to inspect
[logs](Logs.html) in some cases.

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Query;
    ...
    DB db = ...;
    
    // [1] Query user login and name by id 
    int userId = ...;
    Query q1 = select(db, "LOGIN", "NAME")
              .from("USER")
              .where("ID = ?")
              .build(userId); 
              
    // [2] Query distinct passwords in use
    Query q2 = select(db, "PASSWORD")
              .distinct()
              .from("USER")
              .orderBy("PASSWORD")
              .build();
              
    // [3] Get passwords that are used by more than one user and their count.
    Query q3 = select(db, "PASSWORD", "COUNT(*)")
              .from("USER")
              .groupBy("PASSWORD")
              .having("COUNT(*) > 1")
              .build();
              
    // [4] Get pairs of users that have the same password.
    Query q4 = select(db, "u1.LOGIN", "u2.LOGIN")
              .from("USER u1", "USER u2")
              .where("u1.LOGIN <> u2.LOGIN AND u1.PASSWORD = u2.PASSWORD")
              .build();
   
## Summary of methods

### `JDBDT`

* `table(db, tableName)` creates a new `Table` data source.
* `query(db, sql [,args])` creates a new `Query` data source from SQL code.
* `select(db, cols)` creates a new `QueryBuilder`.

### `DataSource`

* `getDB()` returns the database handle.
* `getSQLForQuery()` yields the SQL code used for querying the database.
* `getColumnCount()` returns the number of columns for the data source.
* `getColumnName(i)` returns the name of column with index `i. 

### `Table` 

* `getName()` returns the table name.
* `columns(cols)` specifies the columns of interest.

### `QueryBuilder`

* `from`, `where`, `distinct`, `groupBy`, `orderBy`, `having`: query parameterization methods (see [above](DataSources.html#QueryBuilder)).
* `build([args])` builds the final `Query` with optional arguments `args`.



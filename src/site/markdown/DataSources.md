
# Data sources

`DataSource` objects represent tables and queries that are used for database 
 [setup](DBSetup.html) or [assertions](DBAssertions.html).
 
&nbsp; <a name="Table"></a>
## Tables

&nbsp; <a name="Table_Builder"></a>
### Using table builders

Tables are represented by `Table`, a subclass of `DataSource`. A table is created
using a builder (`TableBuilder`), as returned by the `table` facade method.
In association, the `columns` should be used to specify the table columns of interest,
and the `build` method to build the actual `Table` object in association to a [database handle](DB.html).

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table userTable = table("USER")
	                 .columns("LOGIN", "NAME", "PASSWORD", "CREATED")
	                 .build(db);

&nbsp; <a name="Table_Key"></a>
### Key columns for a table
In addition, if you wish to perform updates and deletes using data sets, `key` can be used to define the columns that form 
the table's primary key (or that in some other form identify each database row uniquely) 

*Illustration*

     Table userTable = table("USER")
	                  .columns("LOGIN", "NAME", "PASSWORD", "CREATED")
	                  .key("LOGIN")
	                  .build(db);

&nbsp; <a name="Query"></a>
## Queries

Queries are represented by `Query`, a subclass of `DataSource`. 
A `Query` object can be created from a raw SQL statements or using a `QueryBuilder`.    
           
&nbsp; <a name="RawQuery"></a>
### Definition from raw SQL 

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

&nbsp; <a name="QueryBuilder"></a>
### Definition using `QueryBuilder`

`QueryBuilder` objects can be used to define queries programmatically.
The `select` facade method creates a query builder that can be parameterized
using a chained sequence of calls. A final call to `build` in 
such a sequence creates a `Query` object for a given database. 
The parameterization methods are the following:

* `from`: defines the `FROM` clause;
* `where`: defines a `WHERE` clause; 
* `distinct`: defines a `DISTINCT` modifier for the query;
* `orderBy`: defines an `ORDER BY` clause;
* `groupBy`: defines  a `GROUP BY` clause;
* `having`: defines a `HAVING` clause.
* `arguments`: supply arguments for the query.

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
    Query q1 = select("LOGIN", "NAME")
              .from("USER")
              .where("ID = ?")
              .arguments(userId)
              .build(db);
              
    // [2] Query distinct passwords in use
    Query q2 = select("PASSWORD")
              .distinct()
              .from("USER")
              .orderBy("PASSWORD")
              .build(db);
              
    // [3] Get passwords that are used by more than one user and their count.
    Query q3 = select("PASSWORD", "COUNT(*)")
              .from("USER")
              .groupBy("PASSWORD")
              .having("COUNT(*) > 1")
              .build(db);
              
    // [4] Get pairs of users that have the same password.
    Query q4 = select("u1.LOGIN", "u2.LOGIN")
              .from("USER u1", "USER u2")
              .where("u1.LOGIN <> u2.LOGIN AND u1.PASSWORD = u2.PASSWORD")
              .build(db);
              
&nbsp; <a name="SummaryOfMethods"></a>
## Summary of API methods

### `JDBDT`

* `table(name)` creates a new `TableBuilder` with table name set to `name`.
* `query(db, sql [,args])` creates a new `Query` data source from SQL code.
* `select(cols)` creates a new `QueryBuilder` with columns set to `cols`.

### `DataSource`

* `getDB()` returns the database handle.
* `getSQLForQuery()` yields the SQL code used for querying the database.
* `getColumnCount()` returns the number of columns for the data source.
* `getColumns()` returns the list of columns.
* `getColumnName(i)` returns the name of the column with index `i`. 

### `Table` 

* `getName()` returns the table name.

### `TableBuilder`


* `columns(cols)` set the table columns to cols.
* `key(cols)` set the key columns to consider (optional).
* `name(t)` sets the table name to `t`.
* `build(db)` builds the desired `Table` for database `db`.

### `QueryBuilder`

* `columns(cols)` sets the query columns to `cols`.
* `from`, `where`, `distinct`, `groupBy`, `orderBy`, `having`, `arguments`: query parameterization methods (see [above](DataSources.html#QueryBuilder)).
* `build(db)` builds the desired `Query` for database `db`.



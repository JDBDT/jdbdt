
# Data sources

`DataSource` objects represent tables and queries that are used for database 
 [setup](DBSetup.html) or [assertions](DBAssertions.html).
 
## Tables

Tables are represented by `Table`, a subclass of `DataSource`, and created
using the `table` facade method in association to a [database handle](DB.html). 
The `columns` method may be used to specify the table columns of interest; all
columns will be considered by default otherwise.


    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Table;
    ...
    DB db = ...;
    Table userTable = table(db, "USER")
	                 .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED");

## Queries

Queries can be created from raw SQL statements or using `QueryBuilder` objects.    
             
### Definition from SQL code

The `query` facade method may be used to define a data source from a plain SQL query.
        
    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DataSource;
    ...
    DB db = ...;
    int idArgument = ...;
    DataSource q = query(db, "SELECT LOGIN,NAME FROM USER WHERE ID > ?", idArgument);

### Definition using `QueryBuilder`

`QueryBuilder` objects can be used to define queries programmatically.
The `select` facade method creates a query builder that can be parameterized
by a chained sequence of calls that ends with a call to `QueryBuilder.build`.

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DataSource;
    ...
    DB db = ...;
    int idArgument = ...;
    DataSource q = select(db, "LOGIN", "NAME")
                  .from("USER")
                  .where("ID > ?")
                  .build(idArgument); 

The `from` and `where` build methods are in correspondence to `FROM` and `WHERE` clauses in SQL,
as illustrated above. Additional parameterization is possible through:

* `distinct` to set the `DISTINCT` modifier for the query;
* `orderBy` to set up an `ORDER BY` clause;
* `groupBy` to set up a `GROUP BY` clause;
* `having` to set up a `HAVING` clause.

The code below illustrates the use of some of these methods.

Note to `orderBy`: [database assertions](DBAssertions.html) are insensitive 
to the order of query results, but `orderBy` may make it easier to inspect
[logs](Logs.html) in some cases.

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DataSource;
    ...
    DB db = ...;
    DataSource q = select(db, "PASSWORD", "COUNT(LOGIN)")
                  .from("USER")
                  .groupBy("PASSWORD")
                  .having("COUNT(LOGIN) > 1")
                  .build();
    
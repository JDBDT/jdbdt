
# Database assertions

JDBDT assertions are of a special kind, called **delta assertions**. 
The idea is that programmers may state the expected incremental changes made to the database,
i.e., the expected database delta. The figure below illustrates the mechanism. 
Starting from state **S**, called the **reference snapshot**, the SUT acts on a database, 
yielding a new database state, **S'**. **S** and **S'** will have in common **unchanged** data **U**,
but will differ by delta **(O,N)**, where **O = S - S'** is **old** data in **S** no longer defined in **S'**, 
and **N = S' - S** is **new** data in **S'**.

![Database delta](images/jdbdt-delta.png)

In line with this scheme, the programming pattern is:

	Define reference snapshot(s) for data source(s) of interest
	theSUT.changesTheDB();
	Call assertion method(s)
	
## Snapshots 

A reference snapshot for a data source can be defined in two ways:

1.  A call to `populate(data)` for an (implicit) `Table` instance (i.e., `data.getSource()`)
will set `data` as the snapshot for the table at stake. Since `populate(data)` resets the full
contents exactly to `data`, by definition it will then be safe to assume it as the correct database state.
2. A call to `takeSnaphot(source)` for any type of data source (`Table`, `Query`, `SQLDataSource`)
will issue a fresh database query, and record the obtained data set as the reference snapshot for that
source.

## Assertion methods 



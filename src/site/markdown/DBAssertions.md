
# Database assertions

JDBDT assertions are of a special kind, called **delta assertions**. 
The idea is that programmers may state the expected incremental changes made to the database,
i.e., the expected database delta. The figure below illustrates the mechanism. 
Starting from state **S**, called the **reference snapshot**, the SUT acts on a database, 
yielding a new database state, **S'**. **S** and **S'** will have in common **unchanged** data 
**U = S &cap; S'**,
but will differ by **&delta; = (O, N)**, where **O = S &minus; S'** is the **old** data in **S** no longer defined in **S'**, 
and **N = S' &minus; S** is the **new** data in **S'**.

![Database delta](images/jdbdt-delta.png)

In line with this scheme, the programming pattern is:

	Define reference snapshot(s) for data source(s) of interest
	theSUT.changesTheDB();
	Call assertion method(s)
	
## Snapshots 

A data source **snapshot** is a data set that is used as reference for subsequent database
assertions. It can be defined in two ways for a data source `s`:

1.  A call to `populate(data)`, s.t. `data.getSource() == s` and `s`is a `Table` instance 
will set `data` as the snapshot for `s`. Since `populate(data)` resets the full table
contents exactly to `data`, by definition it will be safe to assume it as the correct database state.
2. A call to `takeSnaphot(source)`, regardless of the type of `s` (`Table`, `Query`, `SQLDataSource`)
will issue a fresh database query, and record the obtained data set as the snapshot for `s`.

## Assertion methods 

The elementary JDBT assertion method is `assertDelta`. 
An `assertDelta(oldData, newData)` call,
where `oldData` and `newData` are data sets for the same data source `s`,
checks if the database delta is `(oldData,newData)`, as follows:

1. It issues a new database query for `s`.
2. It computes the actual delta between the query's result and the reference snapshot.
3. It verifies if the expected and actual deltas match. If they do not match, `DBAssertionError`
is thrown, and details on mismatched data are logged (unless `DB.Option.LogAssertionErrors` is disabled). 

A number of other assertion methods are defined for convenience, all of which internally reduce 
to `assertDelta`, as follows:

<table border="1">
	<tr>
		<th align="left">Method</th>
		<th align="left">Description</th>
		<th align="center">O</th>
		<th align="center">N</th>
	</tr>
	<tr>
		<td><code>assertDelta([msg,] oldData, newData)</code></td>
	    <td>Asserts that <code>&delta; = (oldData,newData)</code>.</td>
	    <td align="center"><code>oldData</code></td>
	    <td align="center"><code>newData</code></td>
	</tr>
    <tr>
		<td><code>assertDeleted([msg,] data)</code></td>
	    <td>Asserts that <code>data</code> was deleted.</td>
	    <td align="center"><code>data</code></td>
	    <td align="center"><code>&empty;</code></td>
	</tr>
	<tr>
		<td><code>assertInserted([msg, ] data)</code></td>
	    <td>Asserts that <code>data</code> was inserted.</td>
	    <td align="center"><code>&empty;</code></td>
	    <td align="center"><code>data</code></td>
	</tr>
	<tr>
		<td><code>assertUnchanged([msg,] source)</code></td>
	    <td>Asserts no database changes took place.</td>
	    <td align="center"><code>&empty;</code></td>
	    <td align="center"><code>&empty;</code></td>
	</tr>
</table>




# Compatibility

&nbsp; <a name="Drivers"></a>
## JDBC drivers

JDBDT is expected to work with any (sane) JDBC driver.
The JDBDT build currently tests integration with:

* [Derby](https://db.apache.org/derby)
* [H2](http://www.h2database.com)
* [HSQLDB](http://hsqldb.org)
* [MySQL](http://mysql.com)
* [PostgreSQL](http://postgresql.org)
* [SQLite](https://www.sqlite.org) through [xerial's JDBC driver](https://github.com/xerial/sqlite-jdbc)

&nbsp; <a name="KnownIssues"></a>
## Known issues

&nbsp; <a name="KnownIssues_PostgreSQL"></a>
### PostgreSQL


#### Auto-commit off / Rollback on error

If auto-commit is turned **off** for the database connection 
and a SQL statement raises an error
(e.g., an integrity constraint is violated during an insertion), PostgreSQL aborts the transaction and requires an explicit rollback  execute further statements. 
This is a well known issue in PostgreSQL (e.g., see [here](http://postgresql.nabble.com/25P02-current-transaction-is-aborted-commands-ignored-until-end-of-transaction-block-td2174290.html)).
When using JDBDT, this may affect tests that validate invalid uses of a database by the SUT: 

        try {
	      // at this point connection has autocommit turned off
	      call SUT, expecting it to throw a database exception, e.g., 
	                insert data that violates a primary key   
	      fail("exception expected");
	    }
	    catch(SQLException e) { 
	      assertXXX(...);        // e.g. assertUnchanged(...)
	    }
	  
The `assertXXX` JDBDT assertion above (or any other code that issues a database statement for that matter) will fail with the following message
	
	org.postgresql.util.PSQLException: 
	   ERROR: current transaction is aborted, commands ignored until 
	          end of transaction block 

A possible workaround is to issue a rollback statement before any further operations, i.e., before `assertXXX` in the example snippet above.

&nbsp;<a name="KnownIssues_sqlite"></a>
### sqlite


#### Statement reuse

[Statement reuse should be disabled for xerial's JDBC driver for sqlite](DB.html#StatementReuse).

#### Table truncation not supported

[sqlite does not support `TRUNCATE` statements](https://www.sqlite.org/lang.html), so `JDBDT.truncate` will not work.

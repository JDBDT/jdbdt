
# Compatibility

## JDBC drivers
<a name="Drivers"></a>

JDBDT is expected to work with any (sane) JDBC driver.
The JDBDT build currently tests integration with:

* [Derby](https://db.apache.org/derby)
* [H2](http://www.h2database.com)
* [HSQLDB](http://hsqldb.org)
* [MySQL](http://mysql.com)
* [PostgreSQL](http://postgresql.org)
* [SQLite](https://www.sqlite.org) through [xerial's JDBC driver](https://github.com/xerial/sqlite-jdbc)

## Known issues
<a name="KnownIssues"></a>

### PostgreSQL
<a name="KnownIssues_PostgreSQL"></a>

- If auto-commit is turned **off** for the database connection 
and a SQL statement raises an error
(e.g., an integrity constraint is violated during an insertion), PostgreSQL aborts the transaction and requires an explicit rollback  execute further statements. 
This is a well known issue in PostgreSQL (e.g., see [here](http://postgresql.nabble.com/25P02-current-transaction-is-aborted-commands-ignored-until-end-of-transaction-block-td2174290.html)).
When using JDBDT, this may affect tests that validate invalid uses of a database by the SUT: 

	  try {
	    // at this point connection has autocommit turned off
	    call SUT, expecting it to throw a database exception, e.g., 
	      insert data that violates a primary key 
	    fail("exception expected")
	  }
	  catch(SQLException e) { 
	    assertXXX(...);        // e.g. assertUnchanged
	  }
	}

The `assertXXX` method above (or any other code that issues a database statement)  that will fail with the following message
	
	org.postgresql.util.PSQLException: 
	   ERROR: current transaction is aborted, commands ignored until 
	          end of transaction block 

The workaround is to issue a rollback statement (before `assertXXX` in the snippet above). This may be less than perfect if you're using [JDBDT save-points](DBSetup.html#SaveAndRestore) simultaneously.

### sqlite
<a name="KnownIssues_sqlite"></a>

* [Statement reuse should be disabled for xerial's JDBC driver for sqlite](DB.html#StatementReuse).
* sqlite does not support `TRUNCATE` statements, hence `JDBDT.truncate` will not work.

# Database handles

A database handle encapsulates access to a database 
connection.

&nbsp;<a name="Creation"></a>
## Creation and teardown

A database handle is created using the `database` facade method, for instance
supplying as argument a database URL. Once the database handle is no longer required,
internal resources may be freed up using the `teardown` method.

*Illustration*

	import static org.jdbdt.JDBDT.*;
	import org.jdbdt.DB; 
	import java.sql.Connection;
	import java.sql.DriverManager;
	...
	// Creation
	String dbURL = ...;
	DB dbHandle = database("jdbc:myFaveDBEngine://myDB");
	...
	// Tear-down.
	// The second parameter indicates if the underlying 
	// JDBDT connection should be closed or kept open.
	// In this case we close the connection.
	teardown(db, true);

&nbsp;<a name="Configuration"></a>
## Configuration 

Database handle options are defined by the `DB.Option` enumeration.
They may be enabled and disabled using  `enable` and `disable`, respectively. 
The available options relate to logging and a few other features discussed below. 

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.enable(Option.LOG_SETUP);

<a name="Logging"></a>
### Logging


For debugging purposes or report generation, trace output may be written to a [log file](Logs.html).
The following logging options are defined in `DB.Option`

* `LOG_ASSERTION_ERRORS`: log failed assertions;
* `LOG_ASSERTIONS`: log all assertions (passed or failed);
* `LOG_QUERIES`: log the result of database queries;
* `LOG_SETUP`: log database setup operations (data set insertions and SQL setup commands);
* `LOG_SNAPSHOTS`: log database snapshots;
* `LOG_DATABASE_EXCEPTIONS`: log database exceptions.

At creation time, the `LOG_ASSERTION_ERRORS` and `LOG_DATABASE_EXCEPTIONS` options are enabled by default, and the log output is redirected to `System.err`. 
Subsequently, logging options may be enabled/disabled using `enable` / `disable` 
and the output log may be changed using `setLog`.
A call to `enableFullLogging()` enables all logging options at once.

*Illustration*

	import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Log;
    ...
	DB db = database(...);

	// Write log output to a file
	db.setLog(new File("MyLog.jdbdt.xml"));
	
	// Enable all logging options
	db.enableFullLogging();

Note that if you use a `.gz` extension for log files, they will be GZIP-compressed and have a much smaller in size, e.g.,

	db.setLog(new File("MyLog.jdbdt.xml.gz"));

<a name="StatementReuse"></a>
### Statement reuse

A database handle internally reuses `java.sql.PreparedStatement` objects
to avoid re-compiling SQL code, regardless of any statement pooling in place
for the JDBC driver in use,  The scheme is enabled by default and 
it should generally provide a little more efficiency and cause no problems.
For drivers that do not deal well with statement reuse, however,
the `REUSE_STATEMENTS` option should be disabled as follows:

*Illustration*

    import static org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.disable(Option.REUSE_STATEMENTS)

**Known issue**: statement reuse should be disabled for 
[xerial's JDBC driver for sqlite](Compatibility.html#KnownIssues).
No problems were detected for [all other JDBC drivers tested in the JDBDT build](Compatibility.html#Drivers).


<a name="BatchUpdates"></a>
### Batch updates

The `BATCH_UPDATES` option indicates that database insertions should use the JDBC batch update mechanism, unless the JDBC driver in does not support this feature 
(in this case the option will have no effect). The option is enabled by default.

<a name="CaseSensitiveColumnNames"></a>
### Case-sensitive column handling

The `CASE_SENSITIVE_COLUMN_NAMES` option indicates that column names should be handled in case-sensitive manner. This is required if you wish to use double-quotes with column names. If this option is set, the behavior will be more dependent on the database engine in use, so you should make sure you are aware of the engine's policy regarding column names and be consistent with it.

<a name="SummaryOfMethods"></a>
## Summary of API methods

### `JDBDT`

- `database(c)` creates a handle for database connection `c`.
- `database(url)` creates a handle for the given database URL.
- `database(url, user, pass)` creates a handle for the given database URL, user name, and
password.
- `teardown(db, closeConn)` frees up internal resources used by `db`, and also closes the underlying database connection if `closeConn == true`.

### `DB`

- `getConnection()` returns the connection associated to the handle.
- `enable(o1, o2, ...)` enables given options.
- `disable(o1, o2, ...)` disables given options.
- `isEnabled(o)` tests if option `o` is enabled.
- `enableFullLogging()` enables all logging options.
- `setLog(out)` redirects log output to `out`, a `java.io.File` or `java.io.PrintStream`.
- `setMaximumBatchUpdateSize(n)` sets `n` as the maximum number of operations in a batch update.
- `getMaximumBatchUpdateSize()` gets the current setting for the maximum number of operations in a batch update.

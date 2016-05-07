# Database handles

A database handle encapsulates access to a database 
connection.

## Creation  
<a name="Creation"></a>

A database handle is created using the `database` facade method, supplying as argument
`java.sql.Connection` instance.

Example:

	import org.jdbdt.JDBDT.*;
	import org.jdbdt.DB; 
	import java.sql.Connection;
	import java.sql.DriverManager;
	...
	Connection c = DriverManager.getConnection("jdbc:myFavoriteDB://options");
	DB dbHandle = database(c);

## Configuration 
<a name="Configuration"></a>

Database handle options are defined by the `DB.Option` enumeration.
They may be enabled and disabled using  `enable` and `disable`, respectively. 

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.enable(Option.LOG_SETUP);

The available options relate to logging and other features, discussed below. 

### Logging
<a name="Logging"></a>


For debugging purposes or report generation, trace output may be written to a [log file](Logs.html).
The following logging options are defined in `DB.Option`

* `LOG_ASSERTION_ERRORS`: log failed assertions
* `LOG_ASSERTIONS`: log all assertions (passed or failed)
* `LOG_QUERIES`: log the result of database queries;
* `LOG_SETUP`: log database setup operations;
* `LOG_SQL`: log compilation of SQL statements

At creation time, the `DB.Option.LogAssertionErrors` option is enabled by default, 
and log output is set to `System.err`. 
Subsequently, logging options may be enabled/disabled using `enable` / `disable` 
and the output log may be changed using `setLog`.
The `enableFullLogging()` convenience method enables all logging options at once.

	import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Log;
    ...
	DB db = database(...);

	// Write log output to a file
	db.setLog(new File("MyLog.jdbdt.xml"));
	
	// Enable full logging
	db.enableFullLogging();
	
### Statement reuse
<a name="StatementReuse"></a>

A database handle may maintain a pool of reusable `java.sql.PreparedStatement` object
to avoid re-compiling SQL code it needs to execute. This happens whether or not some form of
statement pooling is implemented for the underlying JDBC driver. 

The scheme is enabled by default and, generally, it should provide
more efficiency and cause no problems during statement execution.
A few drivers, however, may not work well with statement reuse.
In those cases, the `REUSE_STATEMENTS` option should be disabled as follows:

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.disable(Option.REUSE_STATEMENTS)

**Known issue**: statement reuse should be disabled for 
[xerial's JDBC driver for sqlite](https://github.com/xerial/sqlite-jdbc). 
No problems were detected for [all other JDBC drivers tested in the JDBDT build](Compatibility.html).


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
	db.enable(Option.LOG_ASSERTION_ERRORS, Option.LOG_INSERTIONS);

The available options relate to logging and other features, discussed below. 

### Logging
<a name="Logging"></a>


For debugging purposes or report generation, trace output may be written to a [log file](Logs.html).
At creation time, by default, the `DB.Option.LogAssertionErrors` option is set, and log output is
set to `System.err`. Subsequently, logging options may be enabled/disabled (as in the snippet above), 
and the output log may be changed using `setLog`.
The `enableFullLogging()` convenience method enables all logging options at once.

	import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Log;
    ...
    Log log = new Log(new File("MyLog.jdbdt.xml"));
	DB db = database(...);
	db.setLog(log);
	db.enableFullLogging();
	
### Statement re-use
<a name="StatementReuse"></a>

A database handle may maintain a pool of reusable `java.sql.PreparedStatement` object
to avoid re-compiling SQL code it needs to execute. This happens whether or not some form of
pooling is implemented for the underlying JDBC driver. 

The scheme is enabled by default and, generally, it should provide
more efficiency and cause no problems during statement execution.
A few drivers, however, may not work well with statement reuse.
In those cases, statement pooling should be disabled as follows:

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.disable(Option.STATEMENT_POOLING)

**Known issue**: statement pooling should be disabled for 
[xerial's JDBC driver for sqlite](https://github.com/xerial/sqlite-jdbc). 
No problems were detected for [all other JDBC drivers tested in the JDBDT build](Compatibility.html).

	
# Database handles

A database handle encapsulates JDBT data for interface with a database 
connection (a `java.sql.Connection` instance), and provides factory 
methods for creating [data sources](DataSources.html). 

## Creation  
<a name="Creation"></a>

A database handled is created using the `database` facade method, supplying as
argument a `java.sql.Connection` instance.

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

Currently, the available options relate to logging and statement polling, discussed below. 

### Logging
<a name="Logging"></a>


For debugging purposes or report generation, trace output for JDBDT operations may be written to a [JDBDT log](Logs.html). At creation time, no logging options are enabled,
and the internal log writes to `System.err`.  Subsequently,
logging options may be set selectively using `enable` (as in the snippet above) or 
all at once using `enableFullLogging()`, and the output log may be changed using `setLog`.

	import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Log;
    ...
    Log log = new Log(new File("MyLog.jdbdt.xml"));
	DB db = database(...);
	db.setLog(log);
	db.enableFullLogging();
	
### Statement pooling and re-use
<a name="StatementPooling"></a>

A database handle may maintain a pool of reusable `java.sql.PreparedStatement` object
to avoid re-compiling SQL statements it executes. This happens whether or not some form of
pooling is implemented for the underlying JDBC driver. 

Statement pooling is enabled by default and, generally, it should provide
more efficiency and cause no problems during statement execution.
A few drivers, however, may do not work well with statement reuse.
In those cases, statement pooling should be disabled as follow:

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.disable(Option.STATEMENT_POOLING)

*Known issue*: statement pooling should be disabled for 
[xerial's JDBC driver for sqlite](https://github.com/xerial/sqlite-jdbc). 
No problems were detected for [all other JDBC drivers tested in the JDBDT build](Compatibility.html).

	
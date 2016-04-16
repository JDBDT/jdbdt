# Database handles

## Summary 

A database handle encapsulates JDBT data for interface with a database 
connection (a `java.sql.Connection` instance), and provides factory 
methods for [data sources](DataSources.html). 

## Creation  

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

Database handle options are defined by the `DB.Option` enumeration.
They may be enabled and disabled using  `enable` and `disable`, respectively. 

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.enable(Option.LOG_ASSERTION_ERRORS, Option.LOG_INSERTIONS);

Currently, the available options relate to logging and statement polling, discussed below. 

### Trace output

For debugging purposes or report generation, trace output for JDBDT operations may be written to a [JDBDT log](Logs.html). At creation time, no logging options are enabled,
and the internal log associates to `System.err`.  After creation,
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
	
### Statement pooling

A database handle, unless otherwise configured,
maintains a `java.sql.PreparedStatement` object pool to avoid re-compiling SQL statements
that are used more than once. This happens whether or not internal pooling is made 
by the underlying JDBC driver in use. 

Statement pooling is enabled by default. Generally, it should provide
more efficiency and cause no problems during JDBDT execution internally.
A few drivers, however, may do not deal well with statement reuse.
In those cases, statement pooling should be disabled as follow:

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.disable(Option.STATEMENT_POOLING)

*Known issue*: statement pooling should be disabled for 
[xerial's JDBC driver for sqlite](https://github.com/xerial/sqlite-jdbc). 


	
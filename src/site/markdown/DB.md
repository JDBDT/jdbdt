# Database handles

## Summary 

A database handle encapsulates JDBT data for interface with a database 
connection (a `java.sql.Connection` instance), and provides factory 
methods for [data sources](DataSources.html). 

## Creation  

A database handled is created using the `database` facade method. 

Example:

	import org.jdbdt.JDBDT.*;
	import org.jdbdt.DB; 
	import java.sql.Connection;
	import java.sql.DriverManager;
	...
	Connection c = DriverManager.getConnection("jdbc:myFavoriteDB://options");
	DB dbHandle = database(c);

## Configuration 

Database handle options are defined by the `DB.Option` enumeration,
They may be enabled and disabled using  `enable` and `disable`, respectively. 
In the current version the available options related to statement pooling and
trace output, discussed below. 


    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...);
	db.enable(Option.LogAssertionErrors, Option.LogInsertions);

### Trace output

For debugging purposes or report generation, trace information for JDBDT operations may be written to a [JDBDT log](Logs.html). At creation time, no logging options are enabled,
and the internal log associates to `System.err`.  
Logging options may be set selectively using `enable` (as in the previous snippet) or 
all at once using `enableFullLogging()`. The output log may be changed using `setLog`.

	import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.Log;
    ...
    Log log = new Log(new File("MyLog.jdbdt.xml"));
	DB db = database(...);
	db.setLog(log);
	db.enableFullLogging();
	
### Statement pooling

A database handle maintains a statement pool to avoid re-compiling SQL statements
that are used more than once. This happens regardless of any internal pooling 
that may or may not be in place for the JDBC driver in use. 

Statement pooling is enabled by default and, in the general case, should provide for
more efficiency and cause no problems. There might be drivers, however, 
which do not deal well with statement re-use, however. A known case is
[xerial's JDBC driver for sqlite](https://github.com/xerial/sqlite-jdbc). 
For this particular driver (or others if needed) statement pooling should be disabled:

    import org.jdbdt.JDBDT.*;
    import org.jdbdt.DB;
    import org.jdbdt.DB.Option;
    ...
	DB db = database(...)
	db.disable(Option.StatementPooling)



	
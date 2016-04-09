
# Getting started

## The JDBDT facade

The `org.jdbdt.JDBDT` utility class is the main facade for the JDBDT 
API. It provides the core methods for database setup, verification,
and API object creation. The following static import 
may be convenient to refer to the API methods concisely.

    import static org.jdbdt.JDBDT.*;

## Overview of functionality

* [Database handlers](DB.html) encapsulate access to a database through 
a JDBC connection. 
* [Data sources](DataSources.html), table and query objects, 
* [Data sets](DataSets.html) can be defined programatically 
for use in database setup and verification.
* [Database setup methods](DBSetup.html) can be used 
to setup database contents.
* [Database assertion methods](DBAssertions.html) can be used
to verify database changes.


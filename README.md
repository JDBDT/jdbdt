# JDBDT  
[![Build status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)

JDBDT (Java Database Delta Testing) is a library for 
Java database application testing.
It provides support for common tasks in 
the definition of software tests (e.g. in JUnit), such as: 

* database setup and teardown 
(table contents setup, removal, truncation, ...);
* programmatic definition of data sets, rather than 
through data files;
* database state assertions, in particular delta 
assertions that specify only the expected changes
to a database table or query resuls 
(rather than a complete data set);
* the use of objects to define database data;

# License

JDBDT is open-source software under the terms of the 
[Eclipse Public License v 1.0](http://www.eclipse.org/legal/epl-v10.html).

# Status

The first version (0.1.0) is expected to be released soon.

# Requirements

## Compilation 

* Maven >= 3.2 
* JDK >= 1.8

## Database engines

JDBDT is expected to work with any database engine with a JDBC driver.
The JDBDT build currently tests integration with the following engines:

* [Derby](https://db.apache.org/derby) 
* [H2](http://www.h2database.com)
* [HSQLDB](http://hsqldb.org)
* [MySQL](http://mysql.com) 
* [PostgreSQL](http://postgresql.org) 
* [SQLite](https://www.sqlite.org) through [xerial's JDBC driver](https://github.com/xerial/sqlite-jdbc)


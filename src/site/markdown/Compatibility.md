# Compatibility

JDBDT is expected to work with any (sane) JDBC driver.
The JDBDT build currently tests integration with the following engines:

* [Derby](https://db.apache.org/derby)
* [H2](http://www.h2database.com)
* [HSQLDB](http://hsqldb.org)
* [MySQL](http://mysql.com)
* [PostgreSQL](http://postgresql.org)
* [SQLite](https://www.sqlite.org) through [xerial's JDBC driver](https://github.com/xerial/sqlite-jdbc)

# Known issues

* [Statement pooling should be disabled for xerial's JDBC driver for sqlite](DB.html#StatementPooling).


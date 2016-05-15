
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

### sqlite

* [Statement reuse should be disabled for xerial's JDBC driver for sqlite](DB.html#StatementReuse).
* The `JDBDT.truncate(Table)` method should not be used, since sqlite does not support TRUNCATE statements.

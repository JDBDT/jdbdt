[![JDBDT](https://raw.githubusercontent.com/edrdo/jdbdt/master/src/site/resources/images/jdbdt-logo.png)](http://jdbdt.org)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](http://jdbdt.org/MIT_License.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt)
[![GitHub release](https://img.shields.io/github/release/edrdo/jdbdt.svg)](https://github.com/edrdo/jdbdt/releases)
[![Travis build Status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)
[![AppVeyor build status](https://ci.appveyor.com/api/projects/status/647d281hp1b8py3p?svg=false)](https://ci.appveyor.com/project/edrdo/jdbdt)
[![Coverity scan](https://scan.coverity.com/projects/13763/badge.svg?flat=1)](https://scan.coverity.com/projects/edrdo-jdbdt)


JDBDT (Java Database Delta Testing) is a library for database test automation.

Visit [http://jdbdt.org](http://jdbdt.org) for reference.

# License

JDBDT is open-source software under the terms of the 
[MIT License](https://opensource.org/licenses/MIT).

Versions prior to 0.12 were released under the terms of the [Eclipse Public License v 1.0](http://www.eclipse.org/legal/epl-v10.html).

Copyright [Eduardo R. B. Marques](http://www.dcc.fc.up.pt/~edrdo), 2016-2017

# Releases

JDBDT releases are available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjdbdt) and [GitHub](https://github.com/edrdo/jdbdt/releases).

# Dependencies

JDBDT is self-contained (it uses the Java 8 SE API only).

# Compilation 

Requirements:

* Maven 3.0 or higher
* Java 8 compiler or higher

Commands: 

        git clone git@github.com:edrdo/jdbdt.git
        cd jdbdt
        mvn install

# Change Log

## 1.0.x

### 1.0.2

* [#31 - Maintenance tasks (1.0.2)](https://github.com/edrdo/jdbdt/issues/31)

### 1.0.1

Maintenance:

* [#30 - Stopped using thread-local data](https://github.com/edrdo/jdbdt/issues/30)

Continuous integration:

* [#29 - Coverity scan during Travis build](https://github.com/edrdo/jdbdt/issues/29)
* [#28 - Fixed broken Java 9 build](https://github.com/edrdo/jdbdt/issues/28) 

### 1.0.0
API:

* [#21 - Data sets can now be used for table updates/deletes.](https://github.com/edrdo/jdbdt/issues/21)
* [#25 - DataSource.getColumns() now available.](https://github.com/edrdo/jdbdt/issues/25)

Continuous integration:

* [#18 - Now using AppVeyor for Windows builds](https://github.com/edrdo/jdbdt/issues/18)
* [#19 - Travis CI: also MacOS builds](https://github.com/edrdo/jdbdt/issues/19)
* [#20 - Travis CI: also Java 9 builds ](https://github.com/edrdo/jdbdt/issues/20)
* [#22 - Travis CI: using mysql service if available](https://github.com/edrdo/jdbdt/issues/22)
* [#23 - Travis CI: using postgresql service if available](https://github.com/edrdo/jdbdt/issues/23)
* [#24 - Travis CI: SonarQube add-on only for standard Linux build](https://github.com/edrdo/jdbdt/issues/24)

Site:
* [#26 - Fixed anchor link location in web pages](https://github.com/edrdo/jdbdt/issues/26)
* [#27 - Start page is now a bit more appealing](https://github.com/edrdo/jdbdt/issues/27)

## 0.12

Slight API adjustments:
* [#9 - API cleanup](https://github.com/edrdo/jdbdt/issues/9)
* [#15 - Let assertTableExists/DoesNotExist take the table name as argument](https://github.com/edrdo/jdbdt/issues/15)
* [#16 - Variant of drop operation supplying database handle and table name](https://github.com/edrdo/jdbdt/issues/16)

Maintenance:
* [#13 - Missing reference documentation in site for table dropping / table existence assertions](https://github.com/edrdo/jdbdt/issues/13)
* [#14 - Start using MIT license from 0.12 onwards](https://github.com/edrdo/jdbdt/issues/14)

## 0.11

Features:
* [#4 - Support for table dropping / table existence assertions](https://github.com/edrdo/jdbdt/issues/4)
* [#10 - Support for compressed log files (GZIP)](https://github.com/edrdo/jdbdt/issues/10)

Maintenance:
* [#6 - SonarQube integration during Travis build](https://github.com/edrdo/jdbdt/issues/6)
* [#7 - Adapt PostgreSQL test code (postgresql-embedded 2.x version features)](https://github.com/edrdo/jdbdt/issues/7)
* [#8 - Let Travis cache Maven repository](https://github.com/edrdo/jdbdt/issues/8)
* [#11 - Optionally run PIT mutation tests](https://github.com/edrdo/jdbdt/issues/11)

## 0.10

* [#3: ColumnFillerException should also extend JDBDTRuntimeException](https://github.com/edrdo/jdbdt/issues/2)
* Misc. maintenance / refactoring / handling of SonarQube issues

## 0.9

* [#2 - DataSource.setSnapshot() clears the contents of previous snapshot set](https://github.com/edrdo/jdbdt/issues/2)
* Exception hiearchy revised: `JDBDTRuntimeException` now base class
for runtime exceptions, new `UnsupportedOperationException` and `InternalErrorException` classes.
* Misc. documentation/site adjustments.

## 0.8

* Builder pattern now more properly used for tables (`TableBuilder`).
* Improved handling of database errors.

## 0.7

* Database insertions now done in batch mode.
* Validation of savepoint support.
* Miscellaneous maintenance (code style, Javadoc, site).

## 0.6

* `populateIfChanged`, `changed`: new facade methods.
* Improved handling of reusable/non-reusable statements.
* Documentation adjustments.

## 0.5

* `DataSetBuilder`: inhibit re-seeding of PRNG, and compute PRNG seed from
data source columns.
* Small adjustements to web site.
* Other small adjustments.

## 0.4

* Logging improvements.

## 0.3 

* Bug fixes & improvements when handling array data (e.g. BINARY) from/to database.
* A few documentation improvements.

## 0.2 

* `DataSet`: `head` and `tail` methods respectively renamed to `first` and `last`.
* `ColumnFillerException` introduced to signal errors during column filler execution.
* Documentation improvements (site pages and Javadoc).

## 0.1

Initial release.



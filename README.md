[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt)
[![Build status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)

# JDBDT 

JDBDT (Java Database Delta Testing) is a library for 
Java database application testing. 

Visit [http://jdbdt.org](http://jdbdt.org) for reference.

# License

JDBDT is open-source software under the terms of the 
[Eclipse Public License v 1.0](http://www.eclipse.org/legal/epl-v10.html).

Copyright [Eduardo R. B. Marques](http://www.dcc.fc.up.pt/~edrdo), 2016-2017

# Download / installation

JDBDT is available at [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjdbdt)  (click link on top of this page for the latest release).

Individual release artifacts are also available at [GitHub](https://github.com/edrdo/jdbdt/releases).

# Dependencies

JDBDT is self-contained: it uses the Java 8 SE API only.

# Compilation 

Requirements:

* Maven 3.2 or higher
* Java 8 compiler or higher

Commands: 

        git clone git@github.com:edrdo/jdbdt.git
        cd jdbdt
        mvn install

# Change Log

## 0.6-SNAPSHOT (current snapshot / next release)

* `populateIfChanged`, `changed`: new facade methods.
* Improved handling of reusable/non-reusable statements.

## 0.5

* `DataSetBuilder: inhibit re-seeding of PRNG, and compute PRNG seed from
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



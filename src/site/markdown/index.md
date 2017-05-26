# JDBDT 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt)
[![GitHub release](https://img.shields.io/github/release/edrdo/jdbdt.svg)](https://github.com/edrdo/jdbdt/releases)
[![Build status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)

JDBDT (Java DataBase Delta Testing) is an open-source Java library for 
testing database applications. The library is designed for automation 
of database setup and validation in test code. The main features are as follows:

* [&delta;-assertions](DBAssertions.html#DeltaAssertions) can be used to verify 
database changes incrementally, in addition to standard
assertions for [database state](DBAssertions.html#StateAssertions) 
or [data set comparison](DBAssertions.html#DataSetAssertions).
* [Setup methods](DBSetup.html) can be used to define database contents. 
* [Data sets](DataSets.html) are defined programmatically,
without need to maintain external "data files". 
* The entire functionality is exposed by a "minimalistic" 
[API facade](Facade.html). JDBDT is also self-contained,
without dependencies from third-party libraries.

For details,  browse the reference documentation 
available in this site, along with the [Javadoc](apidocs/index.html?org/jdbdt/JDBDT.html) for the JDBDT API and the [JDBDT tutorial](Tutorial.html).

## Contribute

The code is hosted at [GitHub](https://github.com/edrdo/jdbdt).
Please use the [issue tracker](https://github.com/edrdo/jdbdt/issues)
to report bugs or propose new features.

## Installation 

**Prerequisite:** JDBDT requires Java 8, it will not work 
with earlier Java versions. 

**Maven Central**

JDBDT is available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjdbdt).

*Maven setup*

	<dependency>
		<groupId>org.jdbdt</groupId>
        <artifactId>jdbdt</artifactId>
        <version>0.12-SNAPSHOT</version>
    </dependency>

*Gradle setup*

     compile 'org.jdbdt:jdbdt:0.12-SNAPSHOT'

Setup instructions for other build systems are available [here](dependency-info.html) .
    
**GitHub**

JDBDT release artifacts are also available
at [GitHub](https://github.com/edrdo/jdbdt/releases).

**Snapshot builds**

To compile and install the latest snapshot from scratch, use
the following commands:

	git clone git@github.com:edrdo/jdbdt.git
	cd jdbdt
	mvn install 

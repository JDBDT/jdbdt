[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt)
[![Build status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)

# JDBDT 

JDBDT (Java DataBase Delta Testing) is an open-source Java library for 
testing database applications, helping you write tests
that include database setup and verification.
The main features are as follows:

* [&delta;-assertions](DBAssertions.html#DeltaAssertions) can be used to verify 
database changes incrementally, in addition to standard
assertions for [database state](DBAssertions.html#StateAssertions) 
or [data set comparison](DBAssertions.html#DataSetAssertions).
* [Setup methods](DBSetup.html) can be used to define database contents. 
* [Data sets](DataSets.html) are defined programmatically,
without need to maintain external "data files". 
* The entire functionality is exposed by a "minimalistic" 
[API facade](Facade.html).

To get started, you may want to check the [tutorial](Tutorial.html).

For help, ask a question in the
[jdbdt-users group](https://groups.google.com/forum/#!forum/jdbdt-users).
To report an issue, use the 
[issue tracker at GitHub](https://github.com/edrdo/jdbdt/issues).

## Installation 

**Prerequisite:** JDBDT requires Java 8, it will not work 
with earlier Java versions. 

JDBDT is available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjdbdt).
Maven users can add the following dependency to a POM file:


	<dependency>
		<groupId>org.jdbdt</groupId>
        <artifactId>jdbdt</artifactId>
        <version>0.2-SNAPSHOT</version>
    </dependency>

To compile and install the latest snapshot from scratch, use
the following commands:

	git clone git@github.com:edrdo/jdbdt.git
	cd jdbdt
	mvn install 
	

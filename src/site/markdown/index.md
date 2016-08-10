# JDBDT 

JDBDT (Java DataBase Delta Testing) is an open-source Java library for 
testing database applications, helping you write tests
that include database setup and verification.
The main features are as follows:

* [Data sets](DataSets.html) are defined programmatically, and can be used
for database [setup](DBSetup.html) or [verification](DBAssertions.html). Thus data sets 
are an integral part of test specifications, 
without no need to maintain external "data set files".
* [&delta;-assertions](DBAssertions.html) can be used to verify 
database changes incrementally, in addition to standard
assertions for database state or data set comparison.
* [Setup](DBSetup.html) methods can be used to manage the contents of database
tables, including insertions, clean-up operations, and 
save-points.
* The entire functionality is exposed by a "minimalistic" 
[API facade](Facade.html).

To get started, you may want to check the [tutorial in this site](Tutorial.html).

## Installation 

**Pre-requisite:** JDBDT requires Java 8, it will not work 
with earlier Java versions. 

JDBDT is available from Maven Central.
If you use Maven (3.2 or later), then add the following dependency to your POM file
to install the latest stable release:

	<dependency>
		<groupId>org.jdbdt</groupId>
        <artifactId>jdbdt</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

To compile and install the latest snapshot from scratch, use
the following commands:

	git clone git@github.com:edrdo/jdbdt.git
	cd jdbdt
	mvn install 
	
## Getting help 

* [JDBDT users group](https://groups.google.com/forum/#!forum/jdbdt-users): for help on using JDBDT, ask a question here. 

* [Github issue tracker](https://github.com/edrdo/jdbdt/issues): for bug reports and feature requests.
 
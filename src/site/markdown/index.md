# JDBDT 

JDBDT (Java DataBase Delta Testing) is a Java library for 
testing database applications, helping you write tests
that include database setup and verification.
The main features are as follows:

* Data sets are defined programmatically, and can be used
for database setup or verification. Hence data sets 
are an integral part of test specifications, 
and there is no need to maintain external "data set files".
* &delta;-assertions can be used to verify 
database changes incrementally, in addition to standard
state assertions.
* Setup methods can be used to manage the contents of database
tables, through insertions, clean-up operations, and 
save-points.

## Installation 

JDBDT is available from Maven Central.
If you use Maven, then add the following dependency to your POM file
to install the latest stable release.

	<dependency>
		<groupId>org.jdbdt</groupId>
        <artifactId>jdbdt</artifactId>
        <version>LATEST</version>
    </dependency>

To compile and install the latest snapshot from scratch, use
the following commands:

	git clone git@github.com:edrdo/jdbdt.git
	cd jdbdt
	mvn install 
	
## Getting help 

* [JDBDT users group](https://groups.google.com/forum/#!forum/jdbdt-users): for help on using JDBDT, ask a question here. 

* [Github issue tracker](https://github.com/edrdo/jdbdt/issues): for bug reports and feature requests.
 

## Compiling from scratch 






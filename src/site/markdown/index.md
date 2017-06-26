# JDBDT 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jdbdt/jdbdt)
[![GitHub release](https://img.shields.io/github/release/edrdo/jdbdt.svg)](https://github.com/edrdo/jdbdt/releases)
[![Build status](https://api.travis-ci.org/edrdo/jdbdt.png?branch=master)](https://travis-ci.org/edrdo/jdbdt)

JDBDT (Java DataBase Delta Testing) is an open-source Java library for 
testing (SQL-based) database applications. The library is designed for automation 
of database setup and validation in test code. 
It has no third-party library dependencies (it just the Java 8 SE API internally), 
making it also easy and lightweight to integrate. 
Compared to existing database testing frameworks, the main conceptual novelty
is the possibility of using [&delta;-assertions](DBAssertions.html#DeltaAssertions).

In a nutshell, the main features are as follows:

* The core functionality is exposed by a simple [API facade](Facade.html).


    import static org.jdbdt.JDBDT.*;


* [Tables and queries](DataSources.html) in association to a [database handle](DB.html) 
can be used as data sources.   


    DB db = database("jdbc:myFaveDBEngine://myDB");
    
    Table userTable = 
      table("USER")
	 .columns("ID", "LOGIN", "NAME", "PASSWORD", "CREATED")
	 .build(db);
	 
	Query idQuery = 
	  select("LOGIN", "NAME")
     .from("USER")
     .where("ID = ?")
     .arguments(userId)
     .build(db);


* [Data sets](DataSets.html) are defined programmatically,
without need to maintain external "data files", for instance using data set builders


    DataSet data = 
       builder(t)
      .sequence("ID", 1) // 1, 2, 3, ...
      .sequence("LOGIN", "harry", "mark", "john")
      .sequence("NAME", "Harry H", "Mark M", "John J")
      .sequence("PASSWORD", i -> "password " + i , 1)
      .random("CREATED", Date.valueOf("2015-01-01"), Date.valueOf("2015-12-31"))
      .generate(3) // generate 3 rows, 
      .sequence("LOGIN", i -> "guest_" + i, 4)  // "user_4", "user_5", ...
      .sequence("NAME", i -> "Guest User " + i, 4) // "Guest User 4", ...
      .value("password", "samePasswordForAllGuests") 
      .generate(6) // 6 more rows keeping ID sequence and CREATED random filler
      .data();   
   
      
* [Setup methods](DBSetup.html) can be used to define database contents, 
for instance to populate tables, clear them, setting & restoring save points, ..., e.g.


    static Table theTable ;
    static DataSet theinitialStata; 
    
    @BeforeClass
    public void globalSetuo() {
      theTable = ... ;
      theInitialData = ...
    }
    
    @Before
    public void perTestSetup() {
       populateIfChanged(initialData);
    }


* [&delta;-assertions](DBAssertions.html#DeltaAssertions) can be used to verify 
database changes incrementally, in addition to standard
assertions for [database state](DBAssertions.html#StateAssertions) 
or [data set comparison](DBAssertions.html#DataSetAssertions), e.g., 

   
    @Test
    public void testUserInsertion() {
      User uJohn = ...;
      DataSet newRow = 
        data(t).row(999, "john", "John", "jpass", Date.valueOf("2016-01-01"));
	  sut.insertOneUser( uJohn ); 
	  // Verify the insertion; assertion fails if other changes are detected
	  assertInserted(newRow); 
	}
	
	@Test
	public void testHarmlessQuery() {
	  User u = sut.getUser("john");
	  ... // standard assertions
	  assertUnchanged(theUserTable); // no delta
    }
    

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
        <version>0.13-SNAPSHOT</version>
    </dependency>

*Gradle setup*

     compile 'org.jdbdt:jdbdt:0.13-SNAPSHOT'

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

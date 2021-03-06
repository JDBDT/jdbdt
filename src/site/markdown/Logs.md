
# Logging format

An XML format is used for [JDBDT log files](DB.html#Logging) and for the output of calls to `JDBDT.dump`. 

<a name="Generic"></a>
## Generic format 

Every JDBDT log message is defined by a `jdbdt-log-message` XML node.
For each node of this type:

* the `version` attribute
identifies the JDBDT version in use;
* the `time` attribute indicates the time of the message;
* `jdbdt-log-message/context` identifies the client call site
related to the message (`context/caller`) and the API method that was invoked (`context/api-method`) -
for both of these, there is information regarding the class, method, file, and line number
(`class`, `method`, `file`, `line`);
* `jdbdt-log-message/data-source` node describes the [data source](DataSources.html) associated to the message, if any - in that case, it informs the source type (`type)`
the SQL code that is executed on each query to the data source (`sql`), plus
what are the columns for the data source at stake (`columns`), where 
each column (`columns/column`) is detailed in terms of its index (`index`), label (`label`),
and SQL type (`sql-type`).

*Illustration*

	<jdbdt-log-message time="..." version="...">
	  <context>
        <caller>
          <class>org.foo.MyTestClass</class>
          <method>myTestMethod</method>
          <file>MyTest.java</file>
          <line>999</line>
        </caller>
        <api-method>
          <class>org.jdbdt.JDBDT</class>
          <method>dump</method>
          <file>JDBDT.java</file>
          <line>541</line>
        </api-method>
      </context>
      <data-source type="Table">
        <columns count="4">
          <column index="1" label="LOGIN" sql-type="VARCHAR"/>
          <column index="2" label="NAME" sql-type="VARCHAR"/>
          <column index="3" label="PASSWORD" sql-type="VARCHAR"/>
          <column index="4" label="CREATED" sql-type="DATE"/>
        </columns>
        <sql><![CDATA[SELECT login, name, password, created FROM Users]]></sql>
      </data-source>

<a name="DataSets"></a>
## Data sets 

A `data-set` node displays the contents of a [data set](DataSets.html) that associates 
to some JDBDT operation (e.g., `populate`, `dump`). 
All rows are detailed in `data-set/rows`. For each column in a row,
a `column` node indicates the column value as well as its
column label (`label` attribute) and Java type (`java-type`).

*Illustration*

	  <data-set>
	    <rows count="4">
	      <row>
    	    <column java-type="java.lang.String" label="LOGIN">root</column>
	        <column java-type="java.lang.String" label="NAME">Root User</column>
	        <column java-type="java.lang.String" label="PASSWORD">I like JDBDT</column>
	        <column java-type="java.sql.Date" label="CREATED">2015-01-01</column>
	      </row>
	      <row>
    	    <column java-type="java.lang.String" label="LOGIN">linus</column>
	        <column java-type="java.lang.String" label="NAME">Linus You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">linux</column>
	        <column java-type="java.sql.Date" label="CREATED">2014-10-31</column>
	      </row>
	      <row>
    	    <column java-type="java.lang.String" label="LOGIN">steve</column>
	        <column java-type="java.lang.String" label="NAME">Steve You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">apple</column>
	        <column java-type="java.sql.Date" label="CREATED">2010-05-30</column>
	      </row>
	      <row>
    	    <column java-type="java.lang.String" label="LOGIN">bill</column>
	        <column java-type="java.lang.String" label="NAME">Bill You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">windows</column>
	        <column java-type="java.sql.Date" label="CREATED">2016-01-01</column>
	      </row>
	    </rows>
	  </data-set>

<a name="StateAndDataSetAssertions"></a>
## State and data set assertions

An `assertion` node refers to a [database state assertion](DBAssertions.html#StateAssertions) or a [data set assertion](DBAssertions.html#DataSetAssertions). It comprises:

* `assertion/expected` in all cases,  detailing the data set
that is either expected for the database state or
for comparison with a given data set.
*  `assertion/errors` if the assertion failed, where 
`assertion/errors/expected` and `assertion/errors/actual` identify 
the mismatch between the expected and actual data sets (matched rows
are not listed).  

*Illustration*

In the fragment below, the assertion error relates to a mismatch between 
the expected and actual values of the `CREATED` column
for the `linus` "user" row, `2015-01-01` (expected) versus `2016-01-01` (actual).
The `steve` and `bill` "users" were matched.

    <assertion>
      <expected count="3">
        <row>
          <column java-type="java.lang.String" label="LOGIN">linus</column>
          <column java-type="java.lang.String" label="NAME">Linus Torvalds</column>
          <column java-type="java.lang.String" label="PASSWORD">linux</column>
          <column java-type="java.sql.Date" label="CREATED">2015-01-01</column>
        </row>
        <row>
          <column java-type="java.lang.String" label="LOGIN">steve</column>
          <column java-type="java.lang.String" label="NAME">Steve Jobs</column>
          <column java-type="java.lang.String" label="PASSWORD">macos</column>
          <column java-type="java.sql.Date" label="CREATED">2015-12-31</column>
        </row>
        <row>
          <column java-type="java.lang.String" label="LOGIN">bill</column>
          <column java-type="java.lang.String" label="NAME">Bill Gates</column>
          <column java-type="java.lang.String" label="PASSWORD">windows</column>
          <column java-type="java.sql.Date" label="CREATED">2015-09-12</column>
        </row>
      </rows>
      <errors>
        <expected count="1">
          <row>
            <column java-type="java.lang.String" label="LOGIN">linus</column>
            <column java-type="java.lang.String" label="NAME">Linus Torvalds</column>
            <column java-type="java.lang.String" label="PASSWORD">linux</column>
            <column java-type="java.sql.Date" label="CREATED">2015-01-01</column>
          </row>
        </expected>
         <actual count="1">
          <row>
            <column java-type="java.lang.String" label="LOGIN">linus</column>
            <column java-type="java.lang.String" label="NAME">Linus Torvalds</column>
            <column java-type="java.lang.String" label="PASSWORD">linux</column>
            <column java-type="java.sql.Date" label="CREATED">2016-01-01</column>
          </row>
        </expected>
      </errors>
    </assertion>

<a name="DeltaAssertions"></a>  
## Delta assertions 

A `delta-assertion` node refers to a [database delta assertion](DBAssertions.html#DeltaAssertions). For an  assertion
where the expected delta is **&delta; = (O, N)** and the actual delta
is **&delta;' = (O', N')**:

* `assertion/expected/old-data` list the entire **O** data set;
* `assertion/expected/new-data` list the entire **N** data set;
* `errors/old-data` list the mismatch between **O** 
(`errors/old-data/expected`) and **O'** (`errors/old-data/actual`);
* `errors/new-data` list the mismatch between **N**
(`errors/new-data/expected`) and **N'** (`errors/new-data/actual`);

Note that, as for state assertions, 
`errors` lists only rows that caused the assertion to fail, i.e.,
matched rows are not listed.

*Illustration*

The fragment below illustrates a failed delta assertion, where no database
changes were expected. Both `expected/old-data` and  `expected/new-data` are empty,
i.e., **O = N = &empty;**, as in a call to `assertUnchanged`. 
The error at stake, identified in `errors/old-data/actual`,
relates to the fact that the entry for "user" `linus` was removed.


	<delta-assertion>
      <expected>
        <old-data count="0"/>
        <new-data count="0"/>
      </expected>
      <errors>
      	<old-data>
          <expected count="0"/>
          <actual count="1">
            <row>
              <column java-type="java.lang.String" label="LOGIN">linus</column>
              <column java-type="java.lang.String" label="NAME">Linus Torvalds</column>
              <column java-type="java.lang.String" label="PASSWORD">linux</column>
              <column java-type="java.sql.Date" label="CREATED">2015-01-01</column>
            </row>
          </actual>
        </old-data>
        <new-data>
          <expected count="0"/>
          <actual count="0"/>
        </new-data>
      </errors>
    </delta-assertion>
    
    <a name="DatabaseExceptions"></a>  
## Database exceptions

A `database-exception` node refers to the stack trace of an `SQLException` thrown by the database engine during the execution of a JDBDT operation. 

*Illustration*

    <database-exception><![CDATA[
    java.sql.SQLSyntaxErrorException: user lacks privilege or object not found: XXX in statement [SELECT XXX FROM Users]
        at org.hsqldb.jdbc.JDBCUtil.sqlException(Unknown Source)
        at org.hsqldb.jdbc.JDBCUtil.sqlException(Unknown Source)
        at org.hsqldb.jdbc.JDBCPreparedStatement.<init>(Unknown Source)
        at org.hsqldb.jdbc.JDBCConnection.prepareStatement(Unknown Source)
        at org.jdbdt.DB.compile(DB.java:276)
    ... [etc]
    ]]></database-exception>



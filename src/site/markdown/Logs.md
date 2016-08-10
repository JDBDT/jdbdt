
# Log / dump format

An XML format is used for [JDBDT log files](DB.html#Logging) and for the output of calls to `JDBDT.dump`. 

## Generic format <a name="Generic"></a>

Every JDBDT log message is defined by a `jdbdt-log-message` XML node.
For each node of this type:

* the `version` attribute
identifies the JDBDT version in use;
* the `time` attribute indicates the time of the message;
* the `context` (sub-)node identifies the client call site
related to the message (`caller`) and the API method that was invoked (`api-method`) -
for both of these, there is information regarding the class, method, file, and line number
(`class`, `method`, `file`, `line`);
*  the `data-source` node describes the [data source](DataSources.html) associated to the message, if any - in that case, it informs the 
the SQL code that is executed on each query to the data source (`sql`), plus
what are the columns for the data source at stake (`columns`), where 
each column (`column`) is detailed in terms of its index (`index`), label (`label`),
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
      <data-source java-type="org.jdbdt.Table">
        <columns count="4">
          <column index="1" label="LOGIN" sql-type="VARCHAR"/>
          <column index="2" label="NAME" sql-type="VARCHAR"/>
          <column index="3" label="PASSWORD" sql-type="VARCHAR"/>
          <column index="4" label="CREATED" sql-type="DATE"/>
        </columns>
        <sql><![CDATA[SELECT login, name, password, created FROM Users]]></sql>
      </data-source>


## Data sets <a name="DataSets"></a>

A `data-set` node displays the contents of a data set that associates 
to some JDBDT operation (e.g., `populate`, `dump`). 
All rows and their count are detailed. For each column in a row,
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


## State and data set assertions

TODO

## Delta assertions

TODO


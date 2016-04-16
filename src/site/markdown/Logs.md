
# Logging

`Log` objects may be used to log JDBDT objects, like [data sets](DataSets.html) or [deltas](DBAssertions.html#Deltas) in an XML format.

A log instance is used internally by a [database handler](DB.html#Logging) instance for tracing operations, and in that case you do not need to interface with the log instance.
For other purposes, e.g. complementary debugging, logs may be created and managed explicitly.

## Creation and use

The creation of a `Log` object takes a `java.io.PrintStream` or `java.io.File` object as argument. A `write` method variant may be used to log a particular JDBDT object.

	import org.jdbdt.Log;
	import org.jdbdt.DataSet;
	import org.jdbdt.Delta;
	import java.io.PrintStream;
	import java.io.File;
	...
	// Creation
	Log log1 = new Log(System.err);
	Log log2 = new Log(new File("MyLog.jdbdt.xml"));
	...
	// Use
	DataSet data = ...;
	log1.write(data);
	
	Delta delta = ...;
	log2.write(delta);
	
## Log format

Each call to a `write` method variant outputs a `jdbdt-log-message` XML node. For instance, the following fragment corresponds to the log output of a [data set object](DataSets.html).

	<jdbdt-log-message time="2016-04-16 13:47:13.14" ...>
	  <data-set>
	    <columns count="5">
	      <column index="1" label="LOGIN" sql-type="INTEGER"/>
	      <column index="2" label="LOGIN" sql-type="VARCHAR"/>
	      <column index="3" label="NAME" sql-type="VARCHAR"/>
	      <column index="4" label="PASSWORD" sql-type="VARCHAR"/>
	      <column index="5" label="CREATED" sql-type="DATE"/>
	    </columns>
	    <rows count="4">
	      <row>
	        <column java-type="java.lang.Integer" label="ID">1</column>
    	    <column java-type="java.lang.String" label="LOGIN">root</column>
	        <column java-type="java.lang.String" label="NAME">Root User</column>
	        <column java-type="java.lang.String" label="PASSWORD">I like JDBDT</column>
	        <column java-type="java.sql.Date" label="CREATED">2015-01-01</column>
	      </row>
	      <row>
	        <column java-type="java.lang.Integer" label="ID">2</column>
    	    <column java-type="java.lang.String" label="LOGIN">linus</column>
	        <column java-type="java.lang.String" label="NAME">Linus You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">linux</column>
	        <column java-type="java.sql.Date" label="CREATED">2014-10-31</column>
	      </row>
	      <row>
	        <column java-type="java.lang.Integer" label="ID">3</column>
    	    <column java-type="java.lang.String" label="LOGIN">steve</column>
	        <column java-type="java.lang.String" label="NAME">Steve You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">apple</column>
	        <column java-type="java.sql.Date" label="CREATED">2010-05-30</column>
	      </row>
	      <row>
	        <column java-type="java.lang.Integer" label="ID">4</column>
    	    <column java-type="java.lang.String" label="LOGIN">bill</column>
	        <column java-type="java.lang.String" label="NAME">Bill You Know Who</column>
	        <column java-type="java.lang.String" label="PASSWORD">windows</column>
	        <column java-type="java.sql.Date" label="CREATED">2016-01-01</column>
	      </row>
	    </rows>
	  </data-set>
	</jdbdt-log-message>


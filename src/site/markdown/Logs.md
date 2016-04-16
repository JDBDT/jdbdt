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
	    <data-source columns="4">
	      <column index="1" label="LOGIN" type="VARCHAR"/>
	      <column index="2" label="NAME" type="VARCHAR"/>
	      <column index="3" label="PASSWORD" type="VARCHAR"/>
	      <column index="4" label="CREATED" type="DATE"/>
	    </data-source>
	    <rows size="4">
	      <row>
	        <column label="LOGIN">alanis</column>
	        <column label="NAME">Alanis</column>
	        <column label="PASSWORD">xyz</column>
	        <column label="CREATED">2015-01-01</column>
	      </row>
	      <row>
	        <column label="LOGIN">linus</column>
	        <column label="NAME">Linus You Know Who</column>
	        <column label="PASSWORD">linux</column>
	        <column label="CREATED">2015-01-01</column>
	      </row>
	      <row>
	        <column label="LOGIN">steve</column>
	        <column label="NAME">Steve You Know Who</column>
	        <column label="PASSWORD">macos</column>
	        <column label="CREATED">2015-12-31</column>
	      </row>
	      <row>
	        <column label="LOGIN">bill</column>
	        <column label="NAME">Bill You Know Who</column>
	        <column label="PASSWORD">windows</column>
	        <column label="CREATED">2015-09-12</column>
	      </row>
	    </rows>
	  </data-set>
	</jdbdt-log-message>


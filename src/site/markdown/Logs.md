# Logging

`Log` objects may be used to log JDBDT objects, like [data sets](DataSets.html) or [deltas](DBAssertions.html#Deltas) in an XML format. 

A log instance is used internally by a [database handler](DB.html#Logging) instance for tracing operations, but may also created explicitly for other purposes (e.g., debugging or report
generation)s.

## Creation and use

The creation of a `Log` object takes a `java.io.PrintStream` or `java.io.File` object as argument. The `write` method may be used to log a particular JDBDT object.

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

Each call to a `Log.write` method variant results in a  `jdbdt-log-message` XML fragment
being output. For instance, the following fragment corresponds to the log output of
a [data set object](DataSets.html).

	<jdbdt-log-message time="2016-04-16 13:47:13.14" ... >
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


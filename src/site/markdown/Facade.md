
# The JDBDT facade


The `org.jdbdt.JDBDT` class  is the facade for the JDBDT API, 
providing the core interface methods for database setup, verification,
and API object creation. 

<a name="StaticImport"></a>
## Static import


The following static import 
may be convenient to refer to the API methods concisely.

    import static org.jdbdt.JDBDT.*;

<a name="Overview"></a>
## Overview of functionality

[(browse Javadoc instead)](apidocs/index.html?org/jdbdt/JDBDT.html)

<table border="1">
  	<tr>
		<th align="left">Functionality</th>
		<th align="left">Summary</th>
		<th align="left">Facade methods</th>
		<th align="left">Related API types</th>
	</tr>
	<tr>
     	<td align="left">
     	  	<a href="DB.html">Database handles</a>
     	</td>
     	<td align="left">
     	  	A database handle mediates access to a database connection.
     	</td>
		<td align="left">
		 	<code>database</code><br/>
		 	<code>teardown</code><br/>
		</td>
		<td align="left">
			<code>DB</code>
		</td>
    </tr>
	<tr>
     	<td align="left">
     		<a href="DataSources.html">Data sources</a>
     	</td>
    	<td align="left">
     	  	A data source represents a database table or query.
     	</td>
		<td align="left">
			<code>query</code><br/>
			<code>select</code><br/>
			<code>table</code><br/>
		</td>
		<td align="left">
			<code>DataSource</code><br/>
			<code>Table</code><br/>
			<code>TableBuilder</code><br/>
			<code>Query</code><br/>
			<code>QueryBuilder</code><br/>			
		</td>
    </tr>
	<tr>
     	<td align="left">
     		<a href="DataSets.html">Data sets</a>
     	</td>
    	<td align="left">
     	  	A data set represents a collection of rows.
     	</td>
		<td align="left">
			<code>builder</code><br/> 
			<code>data</code><br/>
			<code>dump</code><br/>
			<code>empty</code><br/>
		</td>
		<td align="left">
		    <code>Conversion</code><br/>
		    <code>ColumnFiller</code><br/>
			<code>DataSet</code><br/>
			<code>DataSetBuilder</code><br/>
			<code>TypedDataSet</code><br/>
		</td>
    </tr>
    <tr>
     	<td align="left">
     		<a href="DBSetup.html">Database setup</a>
     	</td>
    	<td align="left">
     	  	Setup methods can be used to define the contents of a database.
     	</td>
		<td align="left">
		    <code>changed</code><br/>
			<code>commit</code><br/>
			<code>delete</code><br/>
			<code>deleteAll</code><br/>
			<code>deleteAllWhere</code><br/>
			<code>execute</code><br/>
			<code>drop</code><br/>
			<code>insert</code><br/>
			<code>populate</code><br/>
			<code>populateIfChanged</code><br/>
			<code>restore</code><br/>
			<code>save</code><br/>
			<code>truncate</code><br/>
			<code>update</code><br/>
		</td>
		<td align="left">
		    <code>DataSet</code><br/>
		    <code>DB</code><br/>
			<code>Table</code><br/>
		</td>
    </tr>
    <tr>
     	<td align="left">
     		<a href="DBAssertions.html">Database assertions</a>
     	</td>
    	<td align="left">
     	  	Assertion methods can be used to verify the contents of a database
     	  	or compare data sets.
     	</td>
		<td align="left">
			<code>assertDeleted</code><br/> 
			<code>assertDelta</code><br/>
			<code>assertEmpty</code><br/> 
			<code>assertEquals</code><br/> 
			<code>assertInserted</code><br/> 
			<code>assertState</code><br/>
			<code>assertTableDoesNotExist</code><br/>
			<code>assertTableExists</code><br/>
			<code>assertUnchanged</code><br/> 
			<code>takeSnapshot</code><br/> 
		</td>
		<td align="left">
			<code>DBAssertionError</code><br/>
			<code>DataSet</code><br/>
			<code>DataSource</code><br/>
		</td>
    </tr>
</table>



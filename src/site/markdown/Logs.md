
# Log / dump format

An XML format is used for [JDBDT log files](DB.html#Logging) and for the output of calls to `JDBDT.dump`. The following fragment illustrates the log output for a `dump` call.

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
	  <data-set>
	    <data-source java-type="org.jdbdt.Table">
          <columns count="4">
            <column index="1" label="LOGIN" sql-type="VARCHAR"/>
            <column index="2" label="NAME" sql-type="VARCHAR"/>
            <column index="3" label="PASSWORD" sql-type="VARCHAR"/>
            <column index="4" label="CREATED" sql-type="DATE"/>
          </columns>
          <sql><![CDATA[SELECT login, name, password, created FROM Users]]></sql>
        </data-source>
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
	</jdbdt-log-message>


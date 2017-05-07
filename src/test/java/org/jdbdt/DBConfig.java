package org.jdbdt;


import org.jdbdt.tcfg.hsqlsb.HSQLDBSuite;

@SuppressWarnings("javadoc")
public class DBConfig {

  public static DBConfig getConfig() {
    return INSTANCE;
  }
  
  private static final DBConfig INSTANCE = new DBConfig();
  
  private String url;
  private String driver;    
  private boolean dateSupported;
  private boolean countIsAnInt;
  private boolean reuseStatements;
  
  private DBConfig() {
    reset();
  }
  
  public DBConfig reset() {
    url = HSQLDBSuite.MEM_DB_URL;
    driver = HSQLDBSuite.DRIVER;    
    dateSupported = true;
    countIsAnInt = false;
    reuseStatements = true;
    return this;
  }
  
  public DBConfig setDriver(String driver) {
    this.driver = driver;
    return this;
  }
  
  public String getDriver() {
    return driver;
  }
  
  public DBConfig setURL(String url) {
    this.url = url;
    return this;
  }

  public String getURL() {
    return url;
  }
  
  public DBConfig dateNotSupported() {
    dateSupported = false;
    return this;
  }
  public boolean isDateSupported() {
    return dateSupported;
  }
  
  public DBConfig countReturnsInteger() {
    countIsAnInt = true;
    return this;
  }
  
  public boolean doesCountReturnAnInteger() {
    return countIsAnInt;
  }

  public DBConfig doNotReuseStatements() {
    reuseStatements = false;
    return this;    
  }
  
  public boolean reuseStatements() {
    return reuseStatements;
  }
}

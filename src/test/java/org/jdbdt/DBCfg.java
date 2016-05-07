package org.jdbdt;


import org.jdbdt.tcfg.hsqlsb.HSQLDBSuite;

@SuppressWarnings("javadoc")
public class DBCfg {

  public static DBCfg getConfig() {
    return INSTANCE;
  }
  
  private static final DBCfg INSTANCE = new DBCfg();
  
  private String url;
  private String driver;    
  private boolean dateSupported;
  private boolean countIsAnInt;
  private boolean reuseStatements;
  
  private DBCfg() {
    reset();
  }
  
  public DBCfg reset() {
    url = HSQLDBSuite.MEM_DB_URL;
    driver = HSQLDBSuite.DRIVER;    
    dateSupported = true;
    countIsAnInt = false;
    reuseStatements = true;
    return this;
  }
  
  public DBCfg setDriver(String driver) {
    this.driver = driver;
    return this;
  }
  
  public String getDriver() {
    return driver;
  }
  
  public DBCfg setURL(String url) {
    this.url = url;
    return this;
  }

  public String getURL() {
    return url;
  }
  
  public DBCfg dateNotSupported() {
    dateSupported = false;
    return this;
  }
  public boolean isDateSupported() {
    return dateSupported;
  }
  
  public DBCfg countReturnsInteger() {
    countIsAnInt = true;
    return this;
  }
  
  public boolean doesCountReturnAnInteger() {
    return countIsAnInt;
  }

  public DBCfg doNotReuseStatements() {
    reuseStatements = false;
    return this;    
  }
  
  public boolean reuseStatements() {
    return reuseStatements;
  }
}

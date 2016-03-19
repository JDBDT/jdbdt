package org.jdbdt;


import org.jdbdt.tcfg.hsqlsb.HSQLDBSuite;

@SuppressWarnings("javadoc")
public class DBCfg {

  public static DBCfg getConfig() {
    return INSTANCE;
  }
  
  private static final DBCfg INSTANCE = new DBCfg();
  
  private String url = HSQLDBSuite.MEM_DB_URL;
  private String driver = HSQLDBSuite.DRIVER;    
  private boolean dateSupported = true;
  private boolean countIsAnInt = false;
  
  private DBCfg() {
    reset();
  }
  
  public DBCfg reset() {
    url = HSQLDBSuite.MEM_DB_URL;
    driver = HSQLDBSuite.DRIVER;    
    dateSupported = true;
    countIsAnInt = false;
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

}

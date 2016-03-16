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
  
  public void setDriver(String driver) {
    this.driver = driver;
  }
  
  public void setURL(String url) {
    this.url = url;
  }
  
  public void dateNotSupported() {
    dateSupported = false;
  }
  
  public String getDriver() {
    return driver;
  }
 
  public String getURL() {
    return url;
  }
  
  public boolean isDateSupported() {
    return dateSupported;
  }
}

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
  
  public void setDriver(String driver) {
    this.driver = driver;
  }
  
  public void setURL(String url) {
    this.url = url;
  }
  
  public String getDriver() {
    return driver;
  }
 
  public String getURL() {
    return url;
  }
}

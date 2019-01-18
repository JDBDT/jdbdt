/*
 * The MIT License
 *
 * Copyright (c) 2016-2019 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;


import org.jdbdt.hsqlsb.HSQLDBSuite;

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

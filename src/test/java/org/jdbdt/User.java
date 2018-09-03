/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
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

import java.sql.Date;
import java.util.Arrays;

@SuppressWarnings("javadoc")
public final class User implements Cloneable {

  private String login;
  private String name;
  private String password;
  private Date created;
  
  public User(String login, String name, String password, Date created) {
    setLogin(login);
    setName(name);
    setPassword(password);
    setCreated(created);
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
  public Date getCreated() {
    return created;
  }

  public void setCreated(Date date) {
    this.created = date;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (! (o instanceof User)) {
      return false;
    }
    User other = (User) o;
    return login.equals(other.login)
        && name.equals(other.name)
        && password.equals(other.password)
        && created.equals(other.created);
  }
  
  @Override
  public User clone() {
    try {
      return (User) super.clone();
    } 
    catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

  @Override 
  public int hashCode() {
    return Arrays.hashCode(
      new Object[] { 
         login, name, password, created
      });
  }
}

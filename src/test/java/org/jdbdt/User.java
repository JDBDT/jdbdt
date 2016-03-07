package org.jdbdt;

import java.sql.Date;

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

}

package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("javadoc")
public class UserDAO {
  public static final String TABLE_NAME = "Users";

  public static final String[] COLUMNS = { 
    "login", "name", "password", "created" 
  };

  private final PreparedStatement[] stmts; 

  public UserDAO(Connection c) throws SQLException {
    Op[] ops = Op.values();
    stmts = new PreparedStatement[ops.length];
    try {
      stmts[0] = Op.DROP.compile(c);
      stmts[0].execute();
    } catch(SQLException e) { }
    stmts[1] = Op.CREATE.compile(c);
    stmts[1].execute();
    for (int i = 2; i < ops.length; i++) {
      stmts[i] = ops[i].compile(c);
    }
  }

  private PreparedStatement stmt(Op op) {
    return stmts[op.ordinal()];
  }

  @SafeVarargs
  public final void doInsert(User... users) throws SQLException {
    PreparedStatement s = stmt(Op.INSERT);
    for (User u : users) {
      s.setString(1, u.getLogin());
      s.setString(2, u.getName());
      s.setString(3, u.getPassword());
      s.setDate(4, u.getCreated());
      s.execute();
    }
  }

  public int doDeleteAll() throws SQLException {
    return stmt(Op.DELETE_ALL).executeUpdate();
  }

  @SafeVarargs
  public final int doDelete(String... ids) throws SQLException {
    int n = 0;
    PreparedStatement s = stmt(Op.DELETE);
    for (String id : ids) {
      s.setString(1, id);
      n += s.executeUpdate();
    }
    return n;
  }

  @SafeVarargs
  public final int doUpdate(User... users) throws SQLException {
    int n = 0;
    PreparedStatement s = stmt(Op.UPDATE);
    for (User u : users) {
      s.setString(1, u.getName());
      s.setString(2, u.getPassword());
      s.setDate(3, u.getCreated());
      s.setString(4, u.getLogin());
      n += s.executeUpdate();
    }
    return n;
  }

  public User query(String id) throws SQLException {
    PreparedStatement s = stmt(Op.SELECT);
    s.setString(1, id);
    ResultSet rs = s.executeQuery();
    try {
      return rs.next() ? 
          new User(id, 
              rs.getString(1), 
              rs.getString(2),
              rs.getDate(3)) 
      : null;
    } 
    finally {
      rs.close();
    }
  }

  public int count() throws SQLException {
    ResultSet rs = stmt(Op.COUNT).executeQuery();
    rs.next();
    try {
      return rs.getInt(1);
    } 
    finally {
      rs.close();
    }
  }


  private enum Op { 
    DROP("DROP TABLE " + TABLE_NAME),
    CREATE("CREATE TABLE " + TABLE_NAME + " ("
        + "LOGIN VARCHAR(10) PRIMARY KEY NOT NULL,"
        + "NAME VARCHAR(40) NOT NULL, " + "PASSWORD VARCHAR(32) NOT NULL,"
        + "CREATED DATE)"),
        DELETE_ALL("DELETE FROM " + TABLE_NAME),
        DELETE("DELETE FROM " + TABLE_NAME + " WHERE login = ?"),
        INSERT("INSERT INTO " + TABLE_NAME
            + "(login, name, password, created) VALUES (?,?,?,?)"),
            SELECT("SELECT name, password, created FROM "
                + TABLE_NAME + " WHERE login = ?"),
                UPDATE("UPDATE " + TABLE_NAME 
                    + " set name=?,password=?,created=? WHERE login=?"),
                    COUNT("SELECT COUNT(*) FROM " + TABLE_NAME);

    Op(String sql) {
      this.sql = sql;
    }
    private String sql; 

    PreparedStatement compile(Connection c) throws SQLException {
      return c.prepareStatement(sql);
    }
  }
}

package edu.harvard.lib.librarycloud.collections.model;

import java.util.*;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "user")
public class User implements Principal {

  public User() {}

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private int id;

  @Column(nullable = false)
  private String email;

  private String name;

  @Column(unique = true, nullable = false)
  private String token;

  private String role;

  @Column(name="api_id")
  private int apiId;

 // @ManyToOne(cascade = CascadeType.ALL)
  @Column(name="usertype_id")
  //private UserType userType;
  private int userType;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<UserCollection> collections;

  //getters/setters

  public void setId(int id) { this.id = id; }

  public int getId() {
    return id;
  }

  public List<UserCollection> getCollections() {
    return collections;
  }

  public void setCollections(List<UserCollection> collections) {
    this.collections = collections;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public int getApiId() {
    return apiId;
  }

  public void setApiId(int apiId) {
    this.apiId = apiId;
  }

  public int getUserType() {
        return userType;
    }

  public void setUserType(int userType) {
        this.userType = userType;
    }

  /*public UserType getUserType() {
    return userType;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

   */

  public String getToken() {return token;}

  public void setToken(String token) {
    this.token = token;
  }


}

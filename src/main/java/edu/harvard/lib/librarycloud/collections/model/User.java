package edu.harvard.lib.librarycloud.collections.model;

import java.util.*;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "user")

public class User implements Principal {
	@Id
	@GeneratedValue
	private int id;

	@Column(nullable = false)
	private String email;

	private String name;

	@Column(unique = true, nullable = false)
	private String token;

	private String role;

	@OneToOne
	private UserType userType;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<UserCollection> collections;

	//getters/setters

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

}
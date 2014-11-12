package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "user")

public class User implements Principal
{
	@Id @GeneratedValue
	private int id;

	@Column(nullable = false)
	private String email;

	private String name;

	private String token;

	private String role;

	@OneToMany
	private List<Collection> collections;

	//getters/setters

	public int getId()
	{
		return id;
	}

	public List<Collection> getCollections()
	{
		return collections;
	}

	public void setCollections(List<Collection> collections)
	{
		this.collections = collections;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public void setString(String name)
	{
		this.name = name;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

}
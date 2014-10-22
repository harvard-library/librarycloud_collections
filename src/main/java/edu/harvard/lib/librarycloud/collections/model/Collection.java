package edu.harvard.lib.librarycloud.collections.model;

import javax.persistence.*;

@Entity
public class Collection  {

	public Collection() {}

	@Id @GeneratedValue
	private int id;

	private String title;
	private String summary;

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
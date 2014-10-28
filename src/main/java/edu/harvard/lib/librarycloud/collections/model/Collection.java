package edu.harvard.lib.librarycloud.collections.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Collection  {

	public Collection() {}

	@Id @GeneratedValue
	@XmlElement(name="id")
	private int id;

	@Column(nullable = false)
	private String title;

	@Lob
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
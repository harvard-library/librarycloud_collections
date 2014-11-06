package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="item")
@XmlRootElement(namespace = "http://api.lib.harvard.edu/v2/collection/", name="item")
@XmlType (propOrder={"itemId","collections"})
public class Item  {

	public Item() {}

	@Id @GeneratedValue
	private int id;

	@Column(nullable = false)
	private String itemId;

	@ManyToMany(mappedBy="items")
	private List<Collection> collections;

	public int getId() {
		return id;
	}

	@XmlElement(name="item_id")
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	@XmlElement(name="collections")
	public List<Collection> getCollections() {
		return this.collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}

}
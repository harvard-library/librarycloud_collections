package edu.harvard.lib.librarycloud.collections.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement(namespace = "http://api.lib.harvard.edu/v2/collection/", name="collection")
public class CollectionItem  {

	public CollectionItem() {}

	@Id @GeneratedValue
	private int id;

	@Column(nullable = false)
	private String itemId;

	@ManyToOne()
	@JoinColumn(name="collection_id")
	private Collection collection;

	@XmlElement(name="identifier")
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

	public Collection getCollection() {
		return this.collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

}
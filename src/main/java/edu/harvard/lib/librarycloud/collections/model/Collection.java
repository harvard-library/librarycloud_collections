package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* Implements a subset of the Dublin Core Collections Application Profile (DCCAP)

	Complete list of fields in the DCCAP:

		Type [dc:type]
		Collection Identifier [dc:identifier]
		Title [dc:title]
		Alternative Title [dcterms:alternative]
		Description [dcterms:abstract]
		Size [dcterms:extent]
		Language [dc:language]
		Item Type [cld:itemType]
		Item Format [cld:itemFormat]
		Rights [dc:rights]
		Access Rights [dcterms:accessRights]
		Accrual Method [dcterms:accrualMethod]
		Accrual Periodicity [dcterms:accrualPeriodicity]
		Accrual Policy [dcterms:accrualPolicy]
		Custodial History [dcterms:provenance]
		Audience [dcterms:audience]
		Subject [dc:subject]
		Spatial Coverage [dcterms:spatial]
		Temporal Coverage [dcterms:temporal]
		Date Collection Accumulated [dcterms:created]
		Date Items Created [cld:dateItemsCreated]
		Collector [dc:creator]
		Owner [marcrel:OWN]
		Is Located At [cld:isLocatedAt]
		Is Accessed Via [cld:isAccessedVia]
		Sub-Collection [dcterms:hasPart]
		Super-Collection [dcterms:isPartOf]
		Catalogue or Index [cld:catalogueOrIndex]
		Associated Collection [cld:associatedCollection]
		Associated Publication [dcterms:isReferencedBy]

		TOOD: Include all fields
*/


@Entity
@Table(name="collection")
@XmlRootElement(namespace = "http://api.lib.harvard.edu/v2/collection/", name="collection")
public class Collection  {

	public Collection() {}

	@Id @GeneratedValue
	private int id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="collection_item")
    private List<Item> items;

	@Column(nullable = false)
	private String title;

	@Lob
	private String summary;

	private int extent;

	private String language;

	@Lob
	private String rights;

	@Lob
	private String accessRights;

	@XmlElement(namespace="http://purl.org/dc/elements/1.1", name="identifier")
	public int getId() {
		return id;
	}

	public List<Item> getItems() {
		return items;
	}

	public void addItem(Item item) {

		/* Do not add if the item already exists */
		for (Item i : items) {
			if (i.getItemId().equals(item.getItemId())) {
				return;
			}
		}
		// item.setCollection(this);
		items.add(item);
	}

	public void removeItem(Item item) {
		if (items.contains(item)) {
			items.remove(item);
		}
	}

	@XmlElement(namespace="http://purl.org/dc/elements/1.1", name="type")
	public String getCollection() {
		return "collection";
	}

	@XmlElement(namespace="http://purl.org/dc/elements/1.1/", name="title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement(namespace="http://purl.org/dc/terms/", name="abstract")
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@XmlElement(namespace="http://purl.org/dc/terms", name="extent")
	public int getExtent() {
		return this.extent;
	}

	@XmlElement(namespace="http://purl.org/dc/elements/1.1", name="language")
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@XmlElement(namespace="http://purl.org/dc/elements/1.1", name="rights")
	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	@XmlElement(namespace="http://purl.org/dc/terms", name="accessRights")
	public String getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

}
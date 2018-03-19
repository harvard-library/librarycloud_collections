package edu.harvard.lib.librarycloud.collections.model;

import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.eclipse.persistence.oxm.annotations.XmlInverseReference;



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
    @Transient
    Logger log = Logger.getLogger(Collection.class);

    public static final String ROLE_OWNER = "owner";
    public static final String ROLE_EDITOR = "editor";

    public Collection() {}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified", nullable = false)
    private Date modified;

    @PrePersist
    protected void onCreate() {
    modified = created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
    modified = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="collection_item")
    private List<Item> items;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    private List<UserCollection> users = new ArrayList<>();

    @Column(nullable = false, name="title")
    @Getter @Setter
    private String setName;

    @Getter @Setter
    private boolean dcp;

    @Column(name="is_public")
    @Getter @Setter
    private boolean isPublic;

    @Lob
    @Column(name="set_description")
    @Getter @Setter
    private String setDescription;

    @Column(name="set_spec")
    @Getter @Setter
    private String setSpec;

    @Column(name="thumbnail_urn")
    @Getter @Setter
    private String thumbnailUrn;

    @Column(name="collection_urn")
    @Getter @Setter
    private String collectionUrn;

    @Column(name="base_url")
    @Getter @Setter
    private String baseUrl;

    @Column(name="contact_name")
    @Getter @Setter
    private String contactName;

    @Column(name="contact_department")
    @Getter @Setter
    private String contactDepartment;


    @Transient
    private UserCollection accessRights;

    @XmlElement(name="systemId")

    public int getId() {
        return id;
    }

    public int getSystemId() {
        return id;
    }

    @XmlInverseReference(mappedBy="collections")
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<UserCollection> getUsers() {
        return users;
    }

    public void setUsers(List<UserCollection> users) {
        this.users = users;
    }

    public void addItem(Item item) {
        if(items == null) {
            items = new ArrayList<Item>();
        }
        /* Do not add items with blank ID's */
        if (item.getItemId().trim().isEmpty()) {
            return;
        }
        /* Do not add if the item already exists */
        for (Item i : items) {
            if (i.getItemId().equals(item.getItemId())) {
                return;
            }
        }
        items.add(item);
    }

    public void removeItem(Item item) {
        if (items.contains(item)) {
            items.remove(item);
        }
    }

    // @XmlElement(namespace="http://purl.org/dc/elements/1.1/", name="type")
    // public String getCollection() {
    //     return "collection";
    // }


    // @XmlElement(namespace="http://purl.org/dc/terms", name="extent")
    // public int getExtent() {
    //  return this.extent;
    // }

    // @XmlElement(namespace="http://purl.org/dc/elements/1.1/", name="language")
    // public String getLanguage() {
    //     return language;
    // }

    // public void setLanguage(String language) {
    //     this.language = language;
    // }

    // @XmlElement(namespace="http://purl.org/dc/elements/1.1/", name="rights")
    // public String getRights() {
    //     return rights;
    // }

    // public void setRights(String rights) {
    //   this.rights = rights;
    // }

    // @XmlElement(namespace="http://purl.org/dc/terms/", name="accessRights")
    public UserCollection getAccessRights() {
      return accessRights;
    }

    public void setAccessRights(UserCollection accessRights) {
      this.accessRights = accessRights;
    }

    /* HELPER METHODS */
    public void removeUser(UserCollection uc) {
      if (uc != null) {
        users.remove(uc);
      }
    }

    private UserCollection getUserCollection(User u) {
      if (u != null) {
        List<UserCollection> ucs = this.getUsers();
        if (ucs != null) {
          for (UserCollection uc : ucs) {
            if (uc.getUser().getId() == u.getId()) {
              return uc;
            }
          }
        }
      }
      return null;
    }

}

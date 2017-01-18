package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@Entity
@Table(name="user_collection")
public class UserCollection {

    @Embeddable
    public static class UserCollectionId implements Serializable {

        @Column(name="fk_user")
        protected int user_id;

        @Column(name="fk_collection")
        protected int collection_id;

        public UserCollectionId () {}

        public UserCollectionId(int userId, int collectionId) {
            this.user_id = userId;
            this.collection_id = collectionId;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + user_id;
            hash = hash * 31 + collection_id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;

            UserCollectionId other = (UserCollectionId) obj;

            if (other.collection_id == this.collection_id && other.user_id == this.user_id)
                return true;

            return false;
        }
    }

    public UserCollection() {}

    public UserCollection(User user, Collection collection, Role role) {
        this.id = new UserCollectionId(user.getId(), collection.getId());
        this.user = user;
        this.collection = collection;
        this.role = role;

        collection.getUsers().add(this);
        //user.getCollections().add(this);
    }

    @EmbeddedId
    private UserCollectionId id;

    @ManyToOne
    @JoinColumn(name="fk_user", insertable=false, updatable=false)
    private User user;

    @ManyToOne
    @JoinColumn(name="fk_collection", insertable=false, updatable=false)
    private Collection collection;

    @OneToOne(cascade = CascadeType.ALL)
    @Column(nullable = false)
    private Role role;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

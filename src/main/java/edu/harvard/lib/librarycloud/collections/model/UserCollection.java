package edu.harvard.lib.librarycloud.collections.model;

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

        @Column(name="user_id")
        protected int user_id;

        @Column(name="collection_id")
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
    }

    @EmbeddedId
    private UserCollectionId id;

    @ManyToOne
    @JoinColumn(name="user_id", insertable=false, updatable=false)
    private User user;

    @ManyToOne
    @JoinColumn(name="collection_id", insertable=false, updatable=false)
    private Collection collection;

    @ManyToOne
    @Column(name="role_id", nullable = false)
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

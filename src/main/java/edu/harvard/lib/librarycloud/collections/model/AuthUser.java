package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "auth_user")

public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false)
    private String authSource;

    @Column(nullable = false)
    private String UID;

    @ManyToOne
    @Column(name="user_id")
    private User user;

    public String getAuthSource() {
        return authSource;
    }

    public void setAuthSource(String authSource) {
        this.authSource = authSource;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


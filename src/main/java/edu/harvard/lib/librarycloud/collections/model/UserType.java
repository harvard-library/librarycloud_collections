package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "user_type")

public class UserType {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() { return id; }
}
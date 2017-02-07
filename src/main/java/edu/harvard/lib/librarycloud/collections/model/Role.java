package edu.harvard.lib.librarycloud.collections.model;

import java.util.List;
import javax.persistence.*;
import java.security.*;

@Entity
@Table(name = "role")

public class Role {

    public Role() {
    }

    public Role(String name) {
        setName(name);
    }


    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

}
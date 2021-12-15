package edu.harvard.lib.librarycloud.collections.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * UserApiKey passes back the api key (token) for a new user upon creation
 *
 */

@XmlRootElement(name = "newUser")
public class NewUser {

    private String email;
    private String apiKey;

    NewUser() {}

    public NewUser(String email, String apiKey) {
        this.email = email;
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement(name = "api-key")
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
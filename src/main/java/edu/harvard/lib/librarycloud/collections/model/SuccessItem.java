package edu.harvard.lib.librarycloud.collections.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * ErrorItem creates a java object providing useful error info that can be formatted and
 * thrown as an exception to clients; idea taken from:
 * http://slackspace.de/articles/jersey-how-to-provide-meaningful-exception-messages/
 *
 */

@XmlRootElement(name = "success")
@XmlType(propOrder={"message"})
public class SuccessItem {

    private String message;

    SuccessItem() {}

    public SuccessItem(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
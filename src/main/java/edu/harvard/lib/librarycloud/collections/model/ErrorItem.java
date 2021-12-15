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

@XmlRootElement(name = "error")
@XmlType(propOrder={"status", "message"})
public class ErrorItem {

    private Integer status;
    private String message;

    ErrorItem() {}

    public ErrorItem(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
package edu.harvard.lib.librarycloud.collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import edu.harvard.lib.librarycloud.collections.model.ErrorItem;

/**
 *
 * LibraryCloudException builds an XML/JSON-formatted exception containing code, message, info link
 *
 */

public class LibraryCloudCollectionsException extends WebApplicationException {

    public LibraryCloudCollectionsException(String message, Response.Status status) {
        super(Response.status(status).
                entity(new ErrorItem(status.getStatusCode(), message)).build());
    }
}
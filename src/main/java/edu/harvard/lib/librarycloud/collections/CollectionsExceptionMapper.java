package edu.harvard.lib.librarycloud.collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.WebApplicationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

@Provider
public class CollectionsExceptionMapper implements ExtendedExceptionMapper<Throwable> {
    Logger log = LogManager.getLogger(CollectionsExceptionMapper.class);

    @Override
    public boolean isMappable(Throwable throwable) {
        return true;
        // return !(throwable instanceof WebApplicationException);
    }

    @Override
    public Response toResponse(Throwable t) {
        log.error("Uncaught exception thrown by REST service", t);
        if (t instanceof WebApplicationException) {
            return ((WebApplicationException) t).getResponse();
        } else {
            // TOOD: Provide something better here?
            return Response.serverError().entity("").build();
        }

    }
}

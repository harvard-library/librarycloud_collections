package edu.harvard.lib.librarycloud.collections;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/* Set CORS headers for all API queries. */

@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT");			
		headers.add("Access-Control-Allow-Headers", "Access-Control-Allow-Headers,Content-Type,Accept,X-Requested-With,X-LibraryCloud-API-Key");
		headers.add("Access-Control-Expose-Headers", "Location");

	}

}
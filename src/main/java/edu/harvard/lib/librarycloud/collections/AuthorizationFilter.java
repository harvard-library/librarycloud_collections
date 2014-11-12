package edu.harvard.lib.librarycloud.collections;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import javax.annotation.Priority;
import java.security.*;
import javax.ws.rs.ext.Provider;

import edu.harvard.lib.librarycloud.collections.dao.*;
import edu.harvard.lib.librarycloud.collections.model.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/* Set CORS headers for all API queries. */

@Provider
@Priority(Priorities.AUTHORIZATION)

public class AuthorizationFilter implements ContainerRequestFilter {


	Logger log = Logger.getLogger(AuthorizationFilter.class);
 
    @Autowired
    private CollectionDAO collectionDao;


	public AuthorizationFilter()
	{
		log.debug("Starting AuthorizationFilter");
	}

	@Override
	public void filter(ContainerRequestContext requestContext)
		throws IOException {
		

     // here we set a custom security context
     log.debug("Setting customSecurityContext");

     final ContainerRequestContext finalRequestContext = requestContext;

     finalRequestContext.setSecurityContext(new SecurityContext() {
         @Override
         public Principal getUserPrincipal() {
             MultivaluedMap<String, String> headers = finalRequestContext.getHeaders();
             if(!(headers.containsKey("X-LibraryCloud-API-Key") 
             	&& (headers.getFirst("X-LibraryCloud-API-Key").length() > 0)))
             {
             	log.debug("api key (null): " + headers.getFirst("X-LibraryCloud-API-Key"));
             	return null;
             }

           	log.debug("api key: " + headers.getFirst("X-LibraryCloud-API-Key"));

             User user = collectionDao.getUserForAPIToken(headers.getFirst("X-LibraryCloud-API-Key"));
             return user;
         }

         @Override
         public boolean isUserInRole(String role) {
            User user = (User)this.getUserPrincipal();
            return user != null && user.getRole() == role;
         }

         @Override
         public boolean isSecure() {
             return false;
         }

         @Override
         public String getAuthenticationScheme() {
             return "custom";
         }
     });

	}
}
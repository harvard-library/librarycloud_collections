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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/* Provide token-based authorization filtering  */

@Provider
@Priority(Priorities.AUTHORIZATION)

public class AuthorizationFilter implements ContainerRequestFilter {


  Logger log = LogManager.getLogger(AuthorizationFilter.class);

    @Autowired
    private CollectionDAO collectionDao;

    private static final String APIKEYHEADER = "X-LibraryCloud-API-Key";
    private static final String APIAGENTHEADER = "X-LibraryCloud-API-AGENT";


  public AuthorizationFilter(){
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
                if (!(headers.containsKey(APIKEYHEADER)
                        && (headers.getFirst(APIKEYHEADER).length() > 0))) {
                    log.debug("api key (null): " + headers.getFirst(APIKEYHEADER));
                    return null;
                }

                log.debug("api key: " + headers.getFirst(APIKEYHEADER));

                User user = collectionDao.getUserForAPIToken(headers.getFirst(APIKEYHEADER));
                return user;
            }

            @Override
            public boolean isUserInRole(String role) {
                User user = (User) this.getUserPrincipal();
                return user != null && user.getRole() != null && user.getRole().equals(role);
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

package edu.harvard.lib.librarycloud.collections;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;

import org.apache.log4j.Logger;

@Path("/v2")
public class CollectionsResource {

    Logger log = Logger.getLogger(CollectionsResource.class); 

    @Context 
    UriInfo uriInfo;

    @Context
    HttpServletResponse response;

    /**
     * Get all collections, or collections matching a query
     */
    @GET @Path("collections") 
    @Produces(MediaType.TEXT_PLAIN)
    public String getCollections() {
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (uriInfo.getRequestUri().getQuery() == null) {
            return "Request for all collections!!!!";
        } else {
            return "Request for collections with query " + uriInfo.getRequestUri().getQuery();
        }
    }

    /**
     * Get a collection
     */
    @GET @Path("collections/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCollection(@PathParam("id") String id) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        return "Request for collection with id " + id;
    }

    /**
     * Get items in a collection
     */
    @GET @Path("collections/{id}/items")
    @Produces(MediaType.TEXT_PLAIN)
    public String getItems(@PathParam("id") String id) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        return "Request for items from collection with id " + id;
    }

    /**
     * Create a collection
     */
    @POST @Path("collections")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCollection() {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path("42").build();
        return Response.created(uri).build();        
    }

    /**
     * Update a collection
     */
    @PUT @Path("collections/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCollection(@PathParam("id") String id) {
        return Response.ok().build();        
    }

    /**
     * Delete a collection
     */
    @DELETE @Path("collections/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCollection(@PathParam("id") String id) {
        return Response.ok().build();        
    }

    /**
     * Add item(s) to a collection
     */
    @POST @Path("collections/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addItems() {
        return Response.ok().build();        
    }

    /**
     * Remove items from a collection
     */
    @DELETE @Path("collections/{id}/items/{itemid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeItem(@PathParam("id") String id, 
                               @PathParam("itemid") String item_id) {
        return Response.ok().build();        
    }

}

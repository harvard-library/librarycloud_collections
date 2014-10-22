package edu.harvard.lib.librarycloud.collections;

import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.lib.librarycloud.collections.dao.*;
import edu.harvard.lib.librarycloud.collections.model.*;

@Path("/v2")
public class CollectionsAPI {

    Logger log = Logger.getLogger(CollectionsAPI.class); 

    @Context 
    UriInfo uriInfo;

    @Context
    HttpServletResponse response;

    @Autowired
    private CollectionDAO collectionDao;

    /**
     * Get all collections, or collections matching a query
     */
    @GET @Path("collections") 
    @Produces(MediaType.APPLICATION_JSON)
    public List<Collection> getCollections() {
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (uriInfo.getRequestUri().getQuery() == null) {
            List<Collection> collections = collectionDao.getCollections();
            return collections;
        } else {
            List<Collection> collections = collectionDao.getCollections();
            return collections;
        }
    }

    /**
     * Get a collection
     */
    @GET @Path("collections/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection getCollection(@PathParam("id") Integer id) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        Collection c = collectionDao.getCollection(id);
        if (c == null) {
            throw new NotFoundException();
        }
        return c;
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
    public Response createCollection(Collection collection) {
        Integer id = collectionDao.createCollection(collection);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path(id.toString()).build();
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

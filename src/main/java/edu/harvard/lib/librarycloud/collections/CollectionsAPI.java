package edu.harvard.lib.librarycloud.collections;

import java.net.URI;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;

import org.apache.log4j.Logger;

import org.glassfish.jersey.server.JSONP;
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

    @Autowired
    private CollectionsWorkflow collectionsWorkflow;

    /**
     * Get all collections, or collections matching a query
     */
    @GET @Path("collections") 
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Collection> getCollections(@QueryParam("contains") String contains) {
        List<Collection> collections;
        if (contains != null) {
            collections = collectionDao.getCollectionsForItem(contains);
        } else {
            collections = collectionDao.getCollections();            
        }
        return collections;
    }

    /**
     * Get a collection
     */
    @GET @Path("collections/{id}")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Collection> getCollection(@PathParam("id") Integer id) {
        Collection c = collectionDao.getCollection(id);
        if (c == null) {
            throw new NotFoundException();
        }
        return Collections.singletonList(c);
    }

    /** 
     * Get items, and their associated collections.
     * 'external_ids' is a comma-separated list of canonical item IDs (e.g. the ids of items
     * in the source system)
     */
    @GET @Path("collections/items/{external_ids}")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Item> getItems(@PathParam("external_ids") String external_ids) {
        List<String> external_id_list = new ArrayList<String>(Arrays.asList(external_ids.split(",")));
        if (external_id_list.isEmpty()) {
            throw new NotFoundException();
        }
        List<Item> ci = collectionDao.getItems(external_id_list);
        if (ci == null || ci.isEmpty()) {
            throw new NotFoundException();
        }
        return ci;
    }

    /**
     * Get items in a collection
     */
    @GET @Path("collections/{id}/items")
    @Produces(MediaType.TEXT_PLAIN)
    public String getItemsByCollection(@PathParam("id") String id) {
        return "Request for items from collection with id " + id;
    }

    /**
     * Create a collection
     */
    @POST @Path("collections")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateCollection(@PathParam("id") String id, Collection collection) {
    	
    	Collection result = collectionDao.updateCollection(Integer.parseInt(id),collection);
    	if (result != null) {
            try {
                collectionsWorkflow.notify(result);
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            URI uri = uriBuilder.path(id.toString()).build();
            return Response.created(uri).build();        
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Delete a collection
     */
    @DELETE @Path("collections/{id}")
    public Response deleteCollection(@PathParam("id") Integer id) {
        Collection c = collectionDao.getCollection(id);
        List<Item> items = collectionDao.getItems(c);
        boolean result = collectionDao.deleteCollection(id);
        if (result) {
            try {
                for (Item item : items) {
                    collectionsWorkflow.notify(item.getItemId());
                }
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
        }
        /* Return 204 if successful, 404 if not found. */
        return Response.status(result ? Status.NO_CONTENT : Status.NOT_FOUND).build();        
    }

    /**
     * Add item(s) to a collection
     */
    @POST @Path("collections/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addItems(@PathParam("id") Integer id, List<Item> items) {
        for (Item item : items) {
            boolean result = collectionDao.addToCollection(id, item);
            if (!result) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                try {
                    collectionsWorkflow.notify(item.getItemId());
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }   
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    /**
     * Remove items from a collection
     */
    @DELETE @Path("collections/{id}/items/{itemid}")
    public Response removeItem(@PathParam("id") Integer id, 
                               @PathParam("itemid") String external_item_id) {
        boolean result = collectionDao.removeFromCollection(id, external_item_id);
        if (result) {
            try {
                collectionsWorkflow.notify(external_item_id);    
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
        }
        
        /* Return 204 if successful, 404 if not found. */
        return Response.status(result ? Status.NO_CONTENT : Status.NOT_FOUND).build();        
    }

}

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
    SecurityContext securityContext;

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
    public List<Collection> getCollections(@QueryParam("contains") String contains,@QueryParam("q") String q, 
            @QueryParam("title") String title, @QueryParam("abstract") String summary,
            @QueryParam("limit") Integer limit, @QueryParam("sort") String sort,
            @QueryParam("sort.asc") String sortAsc, @QueryParam("sort.desc") String sortDesc,
            @QueryParam("start") Integer start
            ) {
        List<Collection> collections;
        if (contains != null) {
            collections = collectionDao.getCollectionsForItem(contains);
        } else {
            //handle sorting parameters
            String sortField = "";
            boolean shouldSortAsc = true;
            if(sort != null && sort != ""){
                sortField = sort;
                shouldSortAsc = true;
            } else if(sortAsc != null && sortAsc != ""){
                sortField = sortAsc;
                shouldSortAsc = true;
            } else if(sortDesc != null && sortDesc != ""){
                sortField = sortDesc;
                shouldSortAsc = false;
            }

            if (limit == null || limit <= 0){
                limit = 10; //default to 10
            }

            if ((start == null)){
                start = 0; //default to beginning
            }
            //This is a kludge to handle the frontend/backend column naming.
            //If we find that this happens more often, a translation dictionary
            //should probably be implemented.
            sortField = sortField.replace("abstract", "summary"); 
            try{
                collections = collectionDao.getCollections(q, title, summary, false, limit, sortField, shouldSortAsc, start);
            } catch (Exception e){
                throw new BadRequestException();
            }
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
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Item> getItemsByCollection(@PathParam("id") Integer id) {

        List<Item> results = collectionDao.getItemsByCollection(id);
        return results;
    }
    /**
     * Create a collection
     */
    @POST @Path("collections")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createCollection(Collection collection) {

        User user = (User)securityContext.getUserPrincipal();

        if (user == null) { //user not found.
            return Response.status(Status.UNAUTHORIZED).build();
        }      
        
        Integer id = collectionDao.createCollection(collection, user);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path(id.toString()).build();
        return Response.created(uri).build();        
    }




    /**
     * Update a collection
     */
    @PUT @Path("collections/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateCollection(@PathParam("id") Integer id, Collection collection) {
        if (!this.canEditItems(id)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    	Collection result = collectionDao.updateCollection(id,collection);
    	if (result != null) {
            try {
                collectionsWorkflow.notify(result);
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
            return Response.status(Status.NO_CONTENT).build(); 
        }
    
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Delete a collection
     */
    @DELETE @Path("collections/{id}")
    public Response deleteCollection(@PathParam("id") Integer id) {
        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

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

        if (!this.canEditItems(id)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

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

        if (!this.canEditItems(id)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

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

    /*
   Add or update a user to a collection
    */
    @POST @Path("collections/{id}/user")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addOrUpdateUser(@PathParam("id") Integer id, UserCollection user){
        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        collectionDao.addOrUpdateUserCollection(c, user);

        return Response.status(Status.NOT_FOUND).build();
    }

    /*
    Delete a user for a collection
     */
    @DELETE @Path("collections/{id}/user/{user_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteUser(@PathParam("id") Integer id, @PathParam("user_id") Integer userId) {
        Collection c = collectionDao.getCollection(id);
        UserCollection uc = collectionDao.getUserCollection(userId);

        if (c == null || uc == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        collectionDao.deleteUserCollection(uc);

        return Response.status(Status.NO_CONTENT).build();
    }

    /*
    Get the users for a collection
     */
    @GET @Path("collections/{id}/user")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public  List<UserCollection> getUsers(@PathParam("id") Integer id) {
        Collection c = collectionDao.getCollection(id);

        if (c == null || !this.isOwner(c)) {
            throw new NotFoundException();
        }

        return collectionDao.getUserCollections(c);
    }

    @GET @Path("user/{search}")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<User> getUsers(@PathParam("search") String search) {
        return collectionDao.getUsers(search);
    }

    @GET @Path("role")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Role> getRoles() {
        return collectionDao.getRoles();
    }

    /**
     * Confirm a users access for modifying data.
     */

    private boolean isOwner(Collection c) {
        if (c == null)
            return false;

        User user = (User) securityContext.getUserPrincipal();
        if (user != null) {
            if (isSystemAdmin())
                return true;

            return c.isUserOwner(user);
        }
        return false;
    }

    private boolean canEditItems(Integer collectionId) {
        Collection c = collectionDao.getCollection(collectionId);
        if (c == null)
            return false;

        User user = (User) securityContext.getUserPrincipal();
        if (user != null) {
            if (isSystemAdmin())
                return true;

            return c.canUserEditItems(user);
        }
        return false;
    }

    private boolean isSystemAdmin() {
        return securityContext.isUserInRole("admin");
    }
}

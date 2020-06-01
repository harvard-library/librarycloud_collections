package edu.harvard.lib.librarycloud.collections;

import java.net.URI;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.Math;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.jersey.server.JSONP;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;


import edu.harvard.lib.librarycloud.collections.dao.*;
import edu.harvard.lib.librarycloud.collections.model.*;


@Path("/v2")
public class CollectionsAPI {

    private Logger log = LogManager.getLogger(CollectionsAPI.class);

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

    @Autowired
    private BatchItemProcessor batchItemProcessor;

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
            @QueryParam("start") Integer start,
            @QueryParam("dcp") Boolean dcp
            ) {

        User user = (User)securityContext.getUserPrincipal();

        List<Collection> collections;
        if (contains != null) {
            collections = collectionDao.getCollectionsForItem(user, contains);
        } else {
            //handle sorting parameters
            String sortField = "";
            boolean shouldSortAsc = true;
            if(sort != null && !sort.equals("")){
                sortField = sort;
                shouldSortAsc = true;
            } else if(sortAsc != null && !sortAsc.equals("")){
                sortField = sortAsc;
                shouldSortAsc = true;
            } else if(sortDesc != null && !sortDesc.equals("")){
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

            collections = collectionDao.getCollections(user, q, title, summary, false, limit, sortField, shouldSortAsc, start, dcp);

        }

        return collections;
    }


    /**
     * Get a collection
     */
    @GET @Path("collections/{id}")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Collection> getCollection(
                                          @PathParam("id") Integer id
                                          ) {

        User user = (User)securityContext.getUserPrincipal();

        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            throw new NotFoundException();
        }

        if (c.isPublic()) {
            return Collections.singletonList(c);
        } else if (user != null && collectionDao.isUserOwner(c, user)) {
            return Collections.singletonList(c);
        }

        throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
    }

    /**
     * Get collections for user
     */
    @GET @Path("collections/user")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Collection> getCollection(
                                            @QueryParam("page") Integer page,
                                            @QueryParam("size") Integer size
                                            ) {
        User user = (User)securityContext.getUserPrincipal();
        
        if (user == null) {
            throw new NotFoundException();
        }

        List<Collection> results;
        if (page == null || size == null) {
            results = collectionDao.getCollectionsForUser(user);
        } else {
            PageParams pageParams = new PageParams(page, size);
            results = collectionDao.getCollectionsForUser(user, pageParams);
        }
                
        return results;
    }
    
    /*
        * Get collections for a user's item
    */
    @GET @Path("collections/items/{id}/collections")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Collection> getCollectionsForItem(
                                          @PathParam("id") Integer id
                                          ) {
        User user = (User)securityContext.getUserPrincipal();
        
        if (user == null) {
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }

        if (collectionDao.getItem(id.toString()) == null) {
            throw new LibraryCloudCollectionsException("Item not found", Status.NOT_FOUND);
        }

        List<Collection> collections = collectionDao.getCollectionsForItem(user, id.toString(), false);
        if (collections == null) {
            throw new NotFoundException();
        }
        return collections;
    }

    /**
     * Get items, and their associated collections.
     * 'external_ids' is a comma-separated list of canonical item IDs (e.g. the ids of items
     * in the source system)
     */
    @GET @Path("collections/items/{external_ids}")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Item> getItems(
                               @PathParam("external_ids") String external_ids
                               ) {
        List<String> external_id_list = new ArrayList<>(Arrays.asList(external_ids.split(",")));
        if (external_id_list.isEmpty()) {
            throw new NotFoundException();
        }
        List<Item> ci = collectionDao.getItems(external_id_list);
        if (ci == null || ci.isEmpty()) {
            //throw new NotFoundException();
            log.error("No collections found for list starting: " + external_ids.split(",")[0]);
        }
        return ci;
    }

    /**
     * Get items in a collection
     */
    @GET @Path("collections/{id}/items")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<Item> getItemsByCollection(
                                           @PathParam("id") Integer id,
                                           @QueryParam("page") Integer page,
                                           @QueryParam("size") Integer size
                                           ) {
        List<Item> results;
        if(page == null || size == null) {
            results = collectionDao.getItemsByCollection(id);
        } else {
            PageParams pageParams = new PageParams(page, size);
            results = collectionDao.getItemsByCollection(id, pageParams);
        }
        return results;
    }

    /**
     * Get items in a collection with pagination info
     */
    @GET @Path("collections/{id}/items_paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public Map getItemsByCollectionPaginated(
                                           @PathParam("id") Integer id,
                                           @QueryParam("page") Integer page,
                                           @QueryParam("size") Integer size
                                           ) {
        Map<String, Object> paginatedResults = new HashMap<>();

        List<Item> results;
        PageParams pageParams;
        if(page == null)
            page = 1;

        if(size == null)
            size = 1000;

        pageParams = new PageParams(page, size);
        results = collectionDao.getItemsByCollection(id, pageParams);

        Integer pageCount = (int)Math.floor(collectionDao.getItemCountForCollection(id) / pageParams.getSize()) + 1;

        paginatedResults.put("total_pages", pageCount);
        paginatedResults.put("current_page", pageParams.getPage());
        paginatedResults.put("page_size", pageParams.getSize());
        paginatedResults.put("items", results);

        return paginatedResults;
    }

    /**
     * Get all items in a collection as a file
     */
    @GET @Path("collections/{id}/items_batch_download")
    @Produces({"text/plain"})
    public Response batchDownloadItemsByCollection(
                                           @PathParam("id") final Integer id
                                                   ) {

        StreamingOutput streamOut = new StreamingOutput()
            {
                @Override
                public void write(java.io.OutputStream output) throws IOException, WebApplicationException {
                    try {
                        PrintWriter writer = new PrintWriter(output);
                        boolean doIt = true;
                        PageParams pageParams = new PageParams(0, 1000);

                        List<Item> chunk;

                        while (doIt) {
                            chunk = collectionDao.getItemsByCollection(id, pageParams);
                            if (chunk.size() < 1000) {
                                doIt = false;
                            } else {
                                pageParams.incrementUp();
                            }

                            for (Item item : chunk) {
                                writer.write(item.getItemId() + "\r\n");
                            }
                            writer.flush();
                        }
                    } catch(Exception e) {
                        log.error(e);
                        e.printStackTrace();
                    }
                }
            };

        return Response
            .ok(streamOut, MediaType.APPLICATION_OCTET_STREAM)
            .header("content-disposition", "attachment; filename = items.txt")
            .build();
    }

    /**
     * Create a user
     */
    @POST @Path("collections/users_v1")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createUserV1(User user) {
/*
        User user = (User)securityContext.getUserPrincipal();

        if (user == null) { //user not found.
            return Response.status(Status.UNAUTHORIZED).build();
        }
*/
        Integer id = collectionDao.createUserV1(user);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path(id.toString()).build();
        return Response.created(uri).build();
    }

    @DELETE @Path("collections/users/")
    public Response deleteUser() {
        User user = (User)securityContext.getUserPrincipal();

        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        List<Collection> collections = collectionDao.getCollectionsForUser(user);
        collectionDao.deleteCollections(collections);
        boolean success = true;
        collectionDao.deleteUser(user.getId());
        return Response.status(success ? Status.NO_CONTENT : Status.NOT_FOUND).build();
    }

    /**
     * Create a user and return api key - requires admin role
     */
    @POST @Path("collections/users")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createUser(User newUser) {

        User user = (User)securityContext.getUserPrincipal();

        if (user == null) { //user not found.
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }
        if (!user.getRole().equals("3")) { //user not admin status - TO DO: check on "admin" rather than 3, need method
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }
        try {
            newUser = collectionDao.createUser(newUser);
        } catch (Exception e) {
            if (!collectionDao.doesUserTypeExistByName(newUser.getUserTypeName())) {
                throw new LibraryCloudCollectionsException("Error, incorrect user type. Please use a supported user type", Status.INTERNAL_SERVER_ERROR);
            }
            throw new LibraryCloudCollectionsException("Error, please contact LTS Support", Status.INTERNAL_SERVER_ERROR);
        }
        GenericEntity entity = new GenericEntity<User>(newUser){};
        return Response.ok(entity).build();
    }

    /**
     * Create a collection
     */
    @POST @Path("collections")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createCollection(Collection collection) {

        User user = (User)securityContext.getUserPrincipal();

        if (user == null) { //user not found.
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }

        if (collectionDao.hasUserCreatedMaxAllowedSets(user)) {
            throw new LibraryCloudCollectionsException("You have already created the maximum amount of collections", Status.UNAUTHORIZED);
        }

        if (collectionDao.doesUserAlreadyHaveSetWithTitle(user, collection)) {
            throw new LibraryCloudCollectionsException("A collection with that name already exists", Status.UNAUTHORIZED);
        }

        Integer id = collectionDao.createCollection(collection, user);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path(id.toString()).build();
        //System.out.println("COLL: " + collectionDao.getCollection(id));
        Collection c = collectionDao.getCollection(id);
        GenericEntity entity = new GenericEntity<Collection>(c){};
        return Response.ok(entity).build();
        //return Response.created(uri).build();
    }




    /**
     * Update a collection
     */
    @PUT @Path("collections/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateCollection(
                                     @PathParam("id") Integer id, Collection collection
                                     ) {
        if (!this.canEditItems(id)) {
            //return Response.status(Status.UNAUTHORIZED).build();
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);

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
     * Trigger an Item API update for all items in the set
     */
    @POST @Path("collections/{id}/notify_item_api")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
    public Response notifyItemAPI(
                                  @PathParam("id") Integer id, byte[] bytes
                                  ) {
        Collection c = collectionDao.getCollection(id);
        try {
            collectionsWorkflow.notify(c);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }

        return Response.status(Status.NO_CONTENT).build();
    }

    /**
     * Delete a collection
     */
    @DELETE @Path("collections/{id}")
    public Response deleteCollection(
                                     @PathParam("id") Integer id
                                     ) {
        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        boolean success = true; //think positive
        List<Item> items = collectionDao.getItems(c);

        boolean result;
        // first remove each item and notify SQS
        for (Item item : items) {
            result = collectionDao.removeFromCollection(id, item.getItemId());
            if(success) {
                success = result;
            }
            try {
                collectionsWorkflow.notify(item.getItemId());
            } catch (Exception e) {
                log.error(e);
                success = false;
            }
        }
        // put the items back so they can be deleted by collectionDao :(
        if (success ) {
            success = collectionDao.addToCollection(id, items);
        }

        if (success) {
            success = collectionDao.deleteCollection(id);
        }

        /* Return 204 if successful, 404 if not found. */
        return Response.status(success ? Status.NO_CONTENT : Status.NOT_FOUND).build();
    }

    /**
     * Add item(s) to a collection
     */
    @POST @Path("collections/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addItems(
                             @PathParam("id") Integer id, List<Item> items
                             ) {

        if (!this.canEditItems(id)) {
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }

        for (Item item : items) {
            boolean result = collectionDao.addToCollection(id, item);
            if (!result) {
                System.out.println("No such item: " + item);
                //throw new LibraryCloudCollectionsException("Item Not Found", Status.NOT_FOUND);
            } else {
                try {
                    collectionsWorkflow.notify(item.getItemId());
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }
        Collection c = collectionDao.getCollection(id);
        GenericEntity entity = new GenericEntity<Collection>(c){};
        return Response.ok(entity).build();
        //return Response.status(Status.NO_CONTENT).build();
    }

    /**
     * Remove items from a collection
     */
    @DELETE @Path("collections/{id}/items/{itemid}")
    public Response removeItem(
                               @PathParam("id") Integer id,
                               @PathParam("itemid") String external_item_id
                               ) {

        if (!this.canEditItems(id)) {
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
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


    @POST @Path("collections/{id}/items_batch_upload")
    @Consumes({"multipart/form-data", MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.TEXT_PLAIN)
    public Response batchUploadItems(
                                     @PathParam("id") Integer id,
                                     @FormDataParam("file") InputStream uploadedItemList,
                                     @FormDataParam("file") FormDataContentDisposition fileDetail
                                     ) {

        if (!this.canEditItems(id)) {
            throw new LibraryCloudCollectionsException("Not Authorized", Status.UNAUTHORIZED);
        }

        boolean result;
        try {
            result = batchItemProcessor.addBatch(id, uploadedItemList);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            result = false;
        }
        return Response.status(result ? 200 : 409).build();
    }


    /*
   Add or update a user to a collection
    */
    @POST @Path("collections/{id}/user")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addOrUpdateUserCollection(@PathParam("id") Integer id, UserCollection uc){
        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        boolean result = collectionDao.addOrUpdateUserCollection(c, uc);

        /* Return 204 if successful, 404 if not found. */
        return Response.status(result ? Status.NO_CONTENT : Status.NOT_FOUND).build();
    }

    /*
    Delete a user for a collection
     */
    @DELETE @Path("collections/{id}/user/{user_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteUserCollection(@PathParam("id") Integer id, @PathParam("user_id") Integer userId) {
        Collection c = collectionDao.getCollection(id);


        if (c == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!this.isOwner(c)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        List<UserCollection> ucs = collectionDao.getUserCollections(c);
        if (ucs != null) {
            for (UserCollection uc : ucs) {
                if (uc.getUser().getId() == userId)
                    collectionDao.deleteUserCollection(uc);
            }
        }

        return Response.status(Status.NO_CONTENT).build();
    }

    /*
    Get the users for a collection
     */
    @GET @Path("collections/{id}/user")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public  List<UserCollection> getUserCollections(@PathParam("id") Integer id) {

        Collection c = collectionDao.getCollection(id);

        if (c == null) {
            throw new NotFoundException();
        }
        if (!this.isOwner(c))
            throw new NotAuthorizedException("");

        return collectionDao.getUserCollections(c);
    }

    @GET @Path("users")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<User> getUsers(@QueryParam("q") String search) {

        if (search == null)
            throw new NotFoundException();

        if (!isAuthenticated())
            throw new NotAuthorizedException("");

        return collectionDao.getUsers(search);
    }

    @GET @Path("roles")
    @JSONP(queryParam = "callback")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.9"})
    public List<edu.harvard.lib.librarycloud.collections.model.Role> getRoles() {

        if (!isAuthenticated())
            throw new NotAuthorizedException("");

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

            return collectionDao.isUserOwner(c, user);
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

            return collectionDao.canUserEditItems(c, user);
        }
        return false;
    }

    private boolean isSystemAdmin() {
        return securityContext.isUserInRole("admin");
    }

    private boolean isAuthenticated(){
        return (securityContext.getUserPrincipal() != null);
    }
}

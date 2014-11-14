package edu.harvard.lib.librarycloud.collections.dao;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;

import org.apache.log4j.Logger;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.lib.librarycloud.collections.model.*;

public class CollectionDAO  {
    Logger log = Logger.getLogger(CollectionDAO.class); 

    @PersistenceContext
    private EntityManager em;

	public CollectionDAO() {}

	public List<Collection> getCollections() {
		String query = "SELECT c FROM Collection c";
		List<Collection> result = em.createQuery(query, Collection.class).getResultList();
		return result;
	}

	/**
	 * Get all the collections that contain a particular item
	 * @param  external_item_id ID of the item to look for
	 * @return                  List of Collections containing the item
	 */
	public List<Collection> getCollectionsForItem(String external_item_id) {
		String query = "SELECT DISTINCT c FROM Collection c INNER JOIN c.items i " +
					   "WHERE i.itemId = :external_item_id";
		List<Collection> result = em.createQuery(query, Collection.class)
									 .setParameter("external_item_id", external_item_id)
									 .getResultList();
		return result;
	}

	/**
	 * Get User from API token
	 * @param  token Token of the user to look for
	 * @return                  User for that token.
	 */

	public User getUserForAPIToken(String token)
	{
		String query = "SELECT u FROM User u " +
					   "WHERE u.token = :token";
	try{
		User result = em.createQuery(query, User.class)
									 .setParameter("token", token).setMaxResults(1)
							 .getSingleResult();
		return result;
	} catch (NoResultException e) { return null;}
	}


	/**
	 * Retrieve items and associated collections based on list of IDs
	 * @param  external_id_list List of IDs to lookup. These are IDs from the source system, not internal IDs
	 * @return              	List of matching Item objects with populated Collections
	 */
	public List<Item> getItems(List<String> external_id_list) {
		String query = "SELECT i FROM Item i LEFT JOIN FETCH i.collections WHERE i.itemId IN :external_id_list";
		List<Item> result = em.createQuery(query, Item.class)
								.setParameter("external_id_list", external_id_list)
								.getResultList();
		return result;
	}	

	public Item getItem(String external_id) {
		List<Item> items = getItems(Collections.singletonList(external_id));
		if ((items != null) && (items.size() == 1)) {
			return items.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Retrieve items and associated collections for all items within a collection
	 * @param  collection 		Collection with items to retrieve
	 * @return              	List of matching Item objects with populated Collections
	 */
	public List<Item> getItems(Collection collection) {
		String query = "SELECT i FROM Item i LEFT JOIN FETCH i.collections INNER JOIN i.collections c WHERE c.id = :collectionId";
		List<Item> result = em.createQuery(query, Item.class)
								.setParameter("collectionId", collection.getId())
								.getResultList();
		return result;
	}	


	/**
	 * Retrieve items associated with a collection based on the collectionId.
	 * @param  id Id of the collection.
	 * @return List of matching Item objects.
	 */
	public List<Item> getItemsByCollection(Integer id)
	{
		String query = "select i from Collection c JOIN c.items i WHERE c.id = :collectionId";
		List<Item> result = em.createQuery(query, Item.class)
								.setParameter("collectionId",id)
								.getResultList();
		return result;
	}

	/**
	 * Get an individual collection by ID
	 * @param  id Internal ID of the collection
	 * @return    Populated Collection, or null if not found
	 */
	public Collection getCollection(Integer id) {
		Collection result = null;
		try {
			result = em.find(Collection.class, id);
		} catch (NoResultException e) {
			return null;
		}
		return result;
	}

    @Transactional
	public Integer createCollection(Collection c) {
		em.persist(c);
		em.flush();
		return c.getId();
	}

	@Transactional
	public Collection updateCollection(Integer id, Collection c){
		Collection hydratedCollection;
		try {
			hydratedCollection = em.find(Collection.class, id);
			if (hydratedCollection == null)
				return null;

			List<String> propertiesToCopy = new ArrayList<String>();
			propertiesToCopy.add("title");
			propertiesToCopy.add("summary");
			propertiesToCopy.add("rights");
			propertiesToCopy.add("accessRights");
			propertiesToCopy.add("language");
			
			for (String property : propertiesToCopy) {
				if (PropertyUtils.getProperty(c, property)!= null) {
					PropertyUtils.setProperty(hydratedCollection, property, PropertyUtils.getProperty(c, property));
				}
			}
			em.persist(hydratedCollection);
			em.flush();

		} catch (Exception e) {
			return null;
		}
		return hydratedCollection;	
	}

    @Transactional
	public boolean deleteCollection(Integer id) {
		Collection c;
		c = em.find(Collection.class, id);
		if (c == null) {
			return false;
		}
		em.remove(c);
		return true;
	}

	@Transactional
	public boolean addToCollection(Integer id, Item item) {
		Collection c;
		c = em.find(Collection.class, id);
		if (c == null) {
			return false;
		}
		List<Item> persistentItems = em.createQuery("SELECT i FROM Item i WHERE i.itemId = :external_item_id")
								 		.setParameter("external_item_id", item.getItemId())
								 		.getResultList();
		/* Check whether a matching item already exists. We should never have more than 1 */
		if (persistentItems != null && persistentItems.size() == 1) {
			c.addItem(persistentItems.get(0));
		} else {
			c.addItem(item);
		}
		em.persist(c);
		em.flush();
		return true;
	}

	@Transactional
	public boolean removeFromCollection(Integer id, String external_item_id) {
		Collection c;
		c = em.find(Collection.class, id);
		if (c == null) {
			return false;
		}
		List<Item> items = em.createQuery("SELECT i FROM Item i WHERE i.itemId = :external_item_id")
								 		.setParameter("external_item_id", external_item_id)
								 		.getResultList();

		Item item = null;
		/* There should be at most one item matching the external id */
		if (items != null && items.size() == 1) {
			item = items.get(0);
		}
		if ((item == null) || !item.getCollections().contains(c)) {
			return false;
		}
		c.removeItem(item);
		em.persist(c);
		em.flush();
		return true;
	}

}
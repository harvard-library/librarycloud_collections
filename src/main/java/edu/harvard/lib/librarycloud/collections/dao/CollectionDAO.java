package edu.harvard.lib.librarycloud.collections.dao;


import java.util.List;
import java.util.ArrayList;
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

	public List<Collection> getCollectionsForItem(String external_item_id) {
		String query = "SELECT DISTINCT c FROM Collection c INNER JOIN c.items i " +
					   "WHERE i.itemId = :external_item_id";
		List<Collection> result = em.createQuery(query, Collection.class)
									 .setParameter("external_item_id", external_item_id)
									 .getResultList();
		return result;
	}

	public List<Item> getItems(String ids) {
		String query = "SELECT i FROM Item i LEFT JOIN FETCH i.collections";
		List<Item> result = em.createQuery(query, Item.class).getResultList();
		return result;
	}	

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
			if(hydratedCollection == null)
				return null;

			List<String> propertiesToCopy = new ArrayList<String>();
			propertiesToCopy.add("title");
			propertiesToCopy.add("summary");
			propertiesToCopy.add("rights");
			propertiesToCopy.add("accessRights");
			propertiesToCopy.add("language");
			
			
			for (String property : propertiesToCopy)
			{
				if(PropertyUtils.getProperty(c, property)!= null)
				{
					PropertyUtils.setProperty(hydratedCollection, property, PropertyUtils.getProperty(c, property));
				}
			}
			em.persist(hydratedCollection);
			em.flush();

		} catch(Exception e){
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
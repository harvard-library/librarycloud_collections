package edu.harvard.lib.librarycloud.collections.dao;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import edu.harvard.lib.librarycloud.collections.model.*;

import org.springframework.transaction.annotation.Transactional;

public class CollectionDAO  {

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
	public boolean addToCollection(Integer id, CollectionItem item) {
		Collection c;
		c = em.find(Collection.class, id);
		if (c == null) {
			return false;
		}
		c.addItem(item);
		em.persist(c);
		em.flush();
		return true;
	}

	@Transactional
	public boolean removeFromCollection(Integer id, Integer item_id) {
		Collection c;
		c = em.find(Collection.class, id);
		if (c == null) {
			return false;
		}
		CollectionItem i = em.find(CollectionItem.class, item_id);
		if ((i == null) || !i.getCollection().equals(c)) {
			return false;
		}
		c.removeItem(i);
		em.remove(i);
		em.persist(c);
		em.flush();
		return true;
	}

}
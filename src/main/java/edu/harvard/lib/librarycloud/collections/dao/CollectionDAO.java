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

	public Collection getCollection(Integer id) {
		Collection result = null;
		String query = "SELECT c FROM Collection c where c.id = :id";
		try {
			result = em.createQuery(query, Collection.class)
						.setParameter("id", id)
						.getSingleResult();
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

}
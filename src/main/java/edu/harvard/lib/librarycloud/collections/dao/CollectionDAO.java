package edu.harvard.lib.librarycloud.collections.dao;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;

import org.apache.log4j.Logger;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.lib.librarycloud.collections.model.*;

public class CollectionDAO  {
    private Logger log = Logger.getLogger(CollectionDAO.class);

    @PersistenceContext
    private EntityManager em;

	public CollectionDAO() {}

	public List<Collection> getCollections(User u, String q, String title,
			String summary, boolean exactMatch, Integer limit,
			String sortField, boolean shouldSortAsc, Integer start
			) {

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Collection> criteriaQuery = criteriaBuilder.createQuery(Collection.class);

		List<Predicate> predicateANDList = new LinkedList<>();
		EntityType<Collection> type = em.getMetamodel().entity(Collection.class);

		Root<Collection> collectionRoot = criteriaQuery.from(Collection.class);
		criteriaQuery.select(collectionRoot);

		List<Collection> result;
//		if (!exactMatch) // Exact functionality not currently supported.
//		{
			title = "%" + (title == null? "" : title.replace('*','%')) + "%";
			summary = "%" + (summary == null? "" : summary.replace('*','%')) + "%";
			q = "%" + (q == null? "" : q.replace('*','%')) + "%";
//		}

		if (title != null && title.length() > (exactMatch ? 0 : 2)) {
			log.debug("title = " + title);
			predicateANDList.add(criteriaBuilder.like(collectionRoot.get(type.getDeclaredSingularAttribute("title", String.class)), title));
		}

		if (summary != null && summary.length() > (exactMatch ? 0 : 2)) {
			predicateANDList.add(criteriaBuilder.like(collectionRoot.get(type.getDeclaredSingularAttribute("summary", String.class)), summary));
		} 

		// "q" is never an exact match search
		if (q != null && q.length() > 2) {
			List<Predicate> predicateORList = new LinkedList<>();
			predicateORList.add(criteriaBuilder.like(collectionRoot.get(type.getDeclaredSingularAttribute("title", String.class)), q));
			predicateORList.add(criteriaBuilder.like(collectionRoot.get(type.getDeclaredSingularAttribute("summary", String.class)), q));
			Predicate[] predicateORArray = new Predicate[predicateORList.size()];
			predicateORArray = predicateORList.toArray(predicateORArray);
			predicateANDList.add(criteriaBuilder.or(predicateORArray));
		} 

		if (!sortField.equals("")) {
			if (!shouldSortAsc){
				criteriaQuery.orderBy(criteriaBuilder.desc(collectionRoot.get(type.getDeclaredSingularAttribute(sortField, String.class))));
			} else {
				criteriaQuery.orderBy(criteriaBuilder.asc(collectionRoot.get(type.getDeclaredSingularAttribute(sortField, String.class))));
			}
		}

		Predicate[] predicateANDArray = new Predicate[predicateANDList.size()];
		predicateANDArray = predicateANDList.toArray(predicateANDArray);

		if (predicateANDList.size() > 0) {
			criteriaQuery.where(criteriaBuilder.and(predicateANDArray));
		}

		TypedQuery query = em.createQuery(criteriaQuery);

		query.setMaxResults(limit);
		query.setFirstResult(start);

		result = query.getResultList();

		result = assignRights(result, u);

		return result;
	}

	/**
	 * Get all the collections that contain a particular item
	 * @param  external_item_id ID of the item to look for
	 * @return                  List of Collections containing the item
	 */
	public List<Collection> getCollectionsForItem(User u, String external_item_id) {
		String query = "SELECT DISTINCT c FROM Collection c INNER JOIN c.items i " +
					   "WHERE i.itemId = :external_item_id";
		List<Collection> result = em.createQuery(query, Collection.class)
									 .setParameter("external_item_id", external_item_id)
									 .getResultList();

		result = assignRights(result, u);

		return result;
	}

	/**
	 * Get User from API token
	 * @param  token Token of the user to look for
	 * @return                  User for that token.
	 */

	public User getUserForAPIToken(String token) {
		String query = "SELECT u FROM User u " +
				"WHERE u.token = :token";
		try {
			User result = em.createQuery(query, User.class)
					.setParameter("token", token).setMaxResults(1)
					.getSingleResult();
			return result;
		} catch (NoResultException e) {
			return null;
		}
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
		Collection result;
		try {
			result = em.find(Collection.class, id);
		} catch (NoResultException e) {
			return null;
		}
		return result;
	}

    @Transactional
	public Integer createCollection(Collection c, User u) {
		//first save the collection to generate an Id
		em.persist(c);
		//then get or create the role and assign the role to the user
		Role owner = getOrCreateRole(Collection.ROLE_OWNER);
		UserCollection uc = new UserCollection(u, c, owner);
		c.getUsers().add(uc);
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

			List<String> propertiesToCopy = new ArrayList<>();
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

	/*
	gets Users from the database based on a search string.  Note that only some user characteristics are returned.
	 */
	public List<User> getUsers(String search) {
		String query = "select new User(u.id, u.email, u.name) from User u WHERE u.email like :search OR u.name like :search";
		List<User> result = em.createQuery(query, User.class)
				.setParameter("search", "%" + search + "%")
				.getResultList();
		return result;
	}

	public List<Role> getRoles() {
		String query = "select r from Role r";
		List<Role> result = em.createQuery(query, Role.class)
				.getResultList();
		return result;
	}

	@Transactional
	public Role getOrCreateRole(String name) {
		Role result;
		String query = "SELECT r FROM Role r " +
				"WHERE r.name = :name";
		try {
			 result = em.createQuery(query, Role.class)
					.setParameter("name", name).setMaxResults(1)
					.getSingleResult();

		} catch (NoResultException e) {
			result = new Role(name);
			em.persist(result);
			em.flush();
		}
		return result;
	}

	@Transactional
	public void addOrUpdateUserCollection(Collection c, UserCollection uc) {

	}

	@Transactional
	public void deleteUserCollection(UserCollection uc) {
		em.remove(uc);
	}

	public UserCollection getUserCollection(int id) {
		UserCollection result;
		try {
			result = em.find(UserCollection.class, id);
		} catch (NoResultException e) {
			return null;
		}
		return result;
	}

	public List<UserCollection> getUserCollections(Collection c) {
		String query = "select uc from UserCollection uc WHERE uc.collection.id = :collectionId";
		List<UserCollection> result = em.createQuery(query, UserCollection.class)
				.setParameter("collectionId", c.getId())
				.getResultList();
		return result;
	}

	private List<UserCollection> getUserCollectionsForUser(User u) {
		String query = "select uc from UserCollection uc WHERE uc.user.id = :userId";
		List<UserCollection> result = em.createQuery(query, UserCollection.class)
				.setParameter("userId", u.getId())
				.getResultList();
		return result;
	}

	public boolean canUserEditItems(Collection c, User u) {
		return (getUserCollection(c, u) != null);
	}

	public boolean isUserOwner(Collection c, User u) {
		UserCollection uc = getUserCollection(c, u);
		if (uc != null && uc.getRole().getName().equals(Collection.ROLE_OWNER))
			return true;

		return false;
	}

	private UserCollection getUserCollection(Collection collection, User user) {
		List<UserCollection> ucs = getUserCollectionsForUser(user);
		if (ucs != null) {
			for(UserCollection uc : ucs) {
				if (uc.getCollection().getId() == collection.getId())
					return uc;
			}
		}
		return null;
	}


	private List<Collection> assignRights(List<Collection> coll, User u) {
		//if this is a call secured with a user call, attribute the collections list with the permissions for this user
		if (u != null) {
			List<UserCollection> ucs = getUserCollectionsForUser(u);
			if (ucs != null){
				for(UserCollection uc : ucs) {
					Collection c = uc.getCollection();
					log.debug(c.getTitle() + ":" + c.getId());
					for (Collection innerc : coll) {
						if (innerc.getId() == c.getId()) {
							List<String> accessRights = new ArrayList<>();
							accessRights.add(uc.getRole().getName());
							innerc.setAccessRights(accessRights);
							break;
						}
					}
				}
			}
		}
		return coll;
	}

}
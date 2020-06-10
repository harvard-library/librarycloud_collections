package edu.harvard.lib.librarycloud.collections.test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.sql.Timestamp;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
// import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.flywaydb.core.Flyway;

import javax.persistence.*;
import java.lang.reflect.*;
import javax.sql.DataSource;

import edu.harvard.lib.librarycloud.collections.model.User;
import edu.harvard.lib.librarycloud.collections.model.UserType;
import edu.harvard.lib.librarycloud.collections.model.UserCollection;
import edu.harvard.lib.librarycloud.collections.model.Item;
import edu.harvard.lib.librarycloud.collections.model.Collection;
import edu.harvard.lib.librarycloud.collections.dao.CollectionDAO;
import edu.harvard.lib.librarycloud.collections.dao.PageParams;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( loader=AnnotationConfigContextLoader.class )
@PropertySource("classpath:librarycloud.collections.test.env.properties")
@Transactional
public class CollectionDAOTest {
    @Value( "${max_collections_per_user}" )
    public int maxCollectionsPerUser;

    @Configuration
    @PropertySource("classpath:librarycloud.collections.test.env.properties")
    static class ContextConfiguration {

        @Value( "${db_url}" )
        String dbUrl;

        @Value( "${db_user}" )
        String dbUser;

        @Value( "${db_password}" )
        String dbPassword;

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        @Autowired
        public PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            JpaTransactionManager txManager = new JpaTransactionManager();
            txManager.setEntityManagerFactory(factory);
            return txManager;
        }

        @Bean
        Flyway flyway() {
            Flyway flyway = new Flyway();
            flyway.setLocations("filesystem:src/main/resources/db/migration");
            flyway.setDataSource(dataSource());
            return flyway;
        }


        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan(new String[] { "edu.harvard.lib.librarycloud.collections.model.*" });
            em.setPersistenceProvider(new PersistenceProviderImpl());
            em.setPersistenceUnitName("integration-test");

            return em;
        }


        @Bean
        public DataSource dataSource(){
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(dbUrl);
            dataSource.setUsername(dbUser);
            dataSource.setPassword(dbPassword);
            return dataSource;
        }


        @Bean
        public CollectionDAO collectionDao() {
            CollectionDAO collectionDao = new CollectionDAO();
            return collectionDao;
        }
    }


    @PersistenceContext
    private EntityManager em;
    private static boolean setUpIsDone = false;

    @Autowired
    private CollectionDAO collectionDao;

    @Autowired
    private Flyway fw;

    @Before
    @Rollback(false)
    public void setUpMigration() {
        if(setUpIsDone) {
            return;
        }
;
        fw.clean();
        fw.migrate();

        setUpIsDone = true;
    }


    @Before
    public void setUpTestUser() {
        User u = new User();
        u.setName("Test User");
        u.setEmail("foo@bar.com");

        try {
            Field f = u.getClass().getDeclaredField("token");
            f.setAccessible(true);
            f.set(u, "00000");
        } catch(NoSuchFieldException e) {
            throw new RuntimeException("No Such Field");
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access");
        }

        em.persist(u);
    }

    @Before
    public void setUpUserTypes() {
        UserType ut = new UserType();
        ut.setName("HDC");
        ut.setDescription("Harvard Digital Collections");

        em.persist(ut);
    }

    @Test
    public void testDoesUserTypeExistByName() {
        assertEquals(collectionDao.doesUserTypeExistByName("HDC"), true);
        assertEquals(collectionDao.doesUserTypeExistByName("NonexistantName"), false);
    }

    @Test
    public void testCreateUserAndGetUserById() {
        User u = new User();
        u.setName("Test User 3");
        u.setEmail("anotherTestUser@anotherTestUser.com");
        u.setUserTypeName("HDC");

        User newUser = collectionDao.createUser(u);
        User foundUser = collectionDao.getUserById(newUser.getId());
        assertEquals(foundUser.getName(), "Test User 3");
        assertEquals(foundUser.getToken().length(), 36);
    }

    @Test
    public void testGettingUserByAPIKey() {
        User u1 = collectionDao.getUserForAPIToken("00000");
        assertEquals(u1.getName(), "Test User");

        User nobody = collectionDao.getUserForAPIToken("12345");
        assertEquals(nobody, null);
    }

    @Test
    public void testGettingUserByEmail() {
        User u1 = collectionDao.getUserForEmail("foo@bar.com");
        assertEquals(u1.getName(), "Test User");

        User nobody = collectionDao.getUserForEmail("nobodysEmail@nobodysEmail.com");
        assertEquals(nobody, null);
    }

    @Test
    public void testCreatingAndRetrievingFullCollectionRecords() {
        User u = collectionDao.getUserForAPIToken("00000");
        Collection c = new Collection();
        c.setSetName("title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");
        c.setCollectionUrn("http://coll.com");
        c.setBaseUrl("http://base.com");
        c.setSetSpec("places:cambridge");
        c.setContactName("thomas cole");
        c.setContactDepartment("hudson river school");

        Integer cId = collectionDao.createCollection(c, u);

        c = collectionDao.getCollection(cId);

        assertEquals("title", c.getSetName());
        assertEquals(true, c.isDcp());
        assertEquals(true, c.isPublic());
        assertEquals("abstract", c.getSetDescription());
        assertEquals("http://thumb.com", c.getThumbnailUrn());
        assertEquals("http://coll.com", c.getCollectionUrn());
        assertEquals("http://base.com", c.getBaseUrl());
        assertEquals("places:cambridge", c.getSetSpec());
        assertEquals("thomas cole", c.getContactName());
        assertEquals("hudson river school", c.getContactDepartment());

        List<Collection> collections = collectionDao.getCollectionsForUser(u);
        assertEquals(collections.size(), 1);

        PageParams pageParams = new PageParams(1, 1);
        collections = collectionDao.getCollectionsForUser(u, pageParams);
        assertEquals(collections.size(), 1);

        pageParams = new PageParams(2, 1);
        collections = collectionDao.getCollectionsForUser(u, pageParams);
        assertEquals(collections.size(), 0);
    }

    @Test
    public void testUpdatingCollectionRecords() {
        User u = collectionDao.getUserForAPIToken("00000");

        Timestamp beforeTime = new Timestamp(System.currentTimeMillis());
        Collection c = new Collection();
        c.setSetName("foo");
        c.setPublic(true);

        Integer cId = collectionDao.createCollection(c, u);
        c = collectionDao.getCollection(cId);
        Timestamp createTime = new Timestamp(c.getModified().getTime());

        assertEquals(c.getSetName(), "foo");
        assertTrue(beforeTime.before(createTime));

        c.setSetName("bar");
        c = collectionDao.updateCollection(cId, c);
        Timestamp modifiedTime = new Timestamp(c.getModified().getTime());

        assertEquals(c.getSetName(), "bar");
        assertTrue(createTime.before(modifiedTime));

        c = collectionDao.getCollection(cId);
        assertEquals(c.getSetName(), "bar");
    }

    @Test
    public void testGetCollectionFromUserCollection() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");

        collectionDao.createCollection(c, u);

        List<UserCollection> ucs = collectionDao.getUserCollectionsForUser(u);
        assertEquals(ucs.size(), 1);

        c = collectionDao.getCollectionFromUserCollection(ucs.get(0));
        assertEquals(c.getSetName(), "title");
    }

    @Test
    public void testDeletingCollectionsWithSharedItems() {

        boolean result;
        String c1Title = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String c2Title = Long.toHexString(Double.doubleToLongBits(Math.random()));

        String item1Id = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String item2Id = Long.toHexString(Double.doubleToLongBits(Math.random()));


        User u = collectionDao.getUserForAPIToken("00000");
        Item i1 = new Item();
        i1.setItemId(item1Id);
        Item i2 = new Item();
        i2.setItemId(item2Id);
        Collection c1 = new Collection();
        Collection c2 = new Collection();
        c1.setSetName(c1Title);
        c2.setSetName(c2Title);

        Integer c1Id = collectionDao.createCollection(c1, u);
        Integer c2Id = collectionDao.createCollection(c2, u);

        result = collectionDao.addToCollection(c1Id, i1);
        result = collectionDao.addToCollection(c2Id, i1);
        Integer i1Id = i1.getId();

        List<Item> c1Items = collectionDao.getItemsByCollection(c1Id);
        List<Item> c2Items = collectionDao.getItemsByCollection(c2Id);

        // Check that both collections hold the object
        assertEquals(1, c1Items.size());
        assertEquals(c1Items.get(0).getItemId(), c2Items.get(0).getItemId());

        // Add a second item to collection 2
        result = collectionDao.addToCollection(c2Id, i2);
        Integer i2Id = i2.getId();
        c2Items = collectionDao.getItemsByCollection(c2Id);

        assertEquals(2, c2Items.size());

        // Delete collection 2!
        collectionDao.deleteCollection(c2Id);

        // Make sure collection 1 still has item 1
        c1Items = collectionDao.getItemsByCollection(c1Id);
        assertEquals(1, c1Items.size());
        assertEquals(item1Id, c1Items.get(0).getItemId());

        //ensure i2 was deleted when c2 was deleted
        assertEquals(collectionDao.getItemByInternalId(i2Id), null);
    }


    @Test
    @Rollback
    public void testPagingItems() {
        User u = collectionDao.getUserForAPIToken("00000");
        Collection c = new Collection();
        c.setSetName("has-items");
        Integer cId = collectionDao.createCollection(c, u);

        for(int i = 0; i < 50; ++i) {
            Item it = new Item();
            it.setItemId("Item " + i);
            boolean result = collectionDao.addToCollection(cId, it);
        }

        PageParams params = new PageParams(0, 10);

        List<Item> cItems = collectionDao.getItemsByCollection(cId, params);
        assertEquals(10, cItems.size());
    }

    @Test
    public void testAddingItems() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("foo");

        Integer cId = collectionDao.createCollection(c, u);

        // add a single item
        Item i = new Item();
        i.setItemId("1");

        collectionDao.addToCollection(cId, i);

        assertEquals(collectionDao.getItemsByCollection(cId).get(0).getItemId(), "1");


        // add a list of item ids with 1 duplicate
        List<Item> items = new ArrayList<Item>();
        int x = 0;
        while(x < 100) {
            i = new Item();
            i.setItemId(""+x);
            items.add(i);
            x = x+1;
        }
        collectionDao.addToCollection(cId, items);

        assertEquals(100, collectionDao.getItemsByCollection(cId).size());
    }

    @Test
    public void testGettingItems() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("foo");

        Integer cId = collectionDao.createCollection(c, u);
        List<Item> items = new ArrayList<Item>();

        String unorderedLetters = "bca";
        String orderedLetters = "abc";
        int x = 0;

        while(x < 3) {
            Item i = new Item();
            i.setItemId(String.valueOf(unorderedLetters.charAt(x)));
            items.add(i);
            x = x+1;
        }

        collectionDao.addToCollection(cId, items);

        items = collectionDao.getItemsByCollection(cId);

        Iterator ii = items.iterator();

        x = 0;
        while(ii.hasNext()){
            Item i = (Item)ii.next();
            assertEquals(i.getItemId(), String.valueOf(orderedLetters.charAt(x)));
            x = x+1;
        }
    }


    @Test
    public void testGettingItemCount() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("foo");

        Integer cId = collectionDao.createCollection(c, u);
        List<Item> items = new ArrayList<Item>();

        String unorderedLetters = "bca";
        int x = 0;

        while(x < 3) {
            Item i = new Item();
            i.setItemId(String.valueOf(unorderedLetters.charAt(x)));
            items.add(i);
            x = x+1;
        }

        collectionDao.addToCollection(cId, items);

        Integer itemCount = collectionDao.getItemCountForCollection(cId);

        assertEquals(itemCount, new Integer(3));
    }

    @Test
    public void testGetUserTypeForName() {
        UserType ut = collectionDao.getUserTypeForName("HDC");

        assertEquals(ut.getDescription(), "Harvard Digital Collections");
    }

    @Test
    public void testHasUserCreatedMaxSets() {
        User u = collectionDao.getUserForAPIToken("00000");

        for (int i = 1; i <= maxCollectionsPerUser; i++) {
            Collection c = new Collection();
            c.setSetName("title" + i);
            c.setDcp(true);
            c.setPublic(true);
            c.setSetDescription("abstract");
            c.setThumbnailUrn("http://thumb.com");

            collectionDao.createCollection(c, u);
            if (i < maxCollectionsPerUser) {
                assertEquals(collectionDao.hasUserCreatedMaxAllowedSets(u, maxCollectionsPerUser), false);
            } else {
                assertEquals(collectionDao.hasUserCreatedMaxAllowedSets(u, maxCollectionsPerUser), true);
            }
        }
    }

    @Test
    public void testDoesUserAlreadyHaveCollectionWithTitle() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");

        collectionDao.createCollection(c, u);
        
        c = new Collection();
        c.setSetName("title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");

        assertEquals(collectionDao.doesUserAlreadyHaveSetWithTitle(u, c), true);

        c = new Collection();
        c.setSetName("Different Title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");

        assertEquals(collectionDao.doesUserAlreadyHaveSetWithTitle(u, c), false);
    }

    @Test
    public void testDeleteUser() {
        User u = collectionDao.getUserForAPIToken("00000");

        Collection c = new Collection();
        c.setSetName("title");
        c.setDcp(true);
        c.setPublic(true);
        c.setSetDescription("abstract");
        c.setThumbnailUrn("http://thumb.com");

        Integer cId = collectionDao.createCollection(c, u);
        c = collectionDao.getCollection(cId);

        collectionDao.deleteUser(u.getId());
        u = collectionDao.getUserForAPIToken("00000");
        assertEquals(u, null);
    }
}

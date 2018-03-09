package edu.harvard.lib.librarycloud.collections.test;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
// import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;

import edu.harvard.lib.librarycloud.collections.model.User;
import edu.harvard.lib.librarycloud.collections.model.Item;
import edu.harvard.lib.librarycloud.collections.model.Collection;
import edu.harvard.lib.librarycloud.collections.dao.CollectionDAO;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration("file:src/main/resources/spring/applicationContext.xml")

public class CollectionDAOTest {

    @Autowired
    private CollectionDAO collectionDao;

    @Test
    public void testDeletingCollectionsWithSharedItems() {

        Boolean result;
        String c1Title = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String c2Title = Long.toHexString(Double.doubleToLongBits(Math.random()));

        String item1Id = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String item2Id = Long.toHexString(Double.doubleToLongBits(Math.random()));


        User u = collectionDao.getUserForAPIToken("555555555");
        Item i1 = new Item();
        i1.setItemId(item1Id);
        Item i2 = new Item();
        i2.setItemId(item2Id);
        Collection c1 = new Collection();
        Collection c2 = new Collection();
        c1.setTitle(c1Title);
        c2.setTitle(c2Title);

        Integer c1Id = collectionDao.createCollection(c1, u);
        Integer c2Id = collectionDao.createCollection(c2, u);


        System.out.println(c1.getId());
        System.out.println(c2.getId());

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

        //TODO - make sure it's gone

        // Make sure collection 1 still has item 1
        c1Items = collectionDao.getItemsByCollection(c1Id);
        assertEquals(1, c1Items.size());
        assertEquals(item1Id, c1Items.get(0).getItemId());

        //ensure i2 was deleted when c2 was deleted
        assertEquals(collectionDao.getItemByInternalId(i2Id), null);
    }
}

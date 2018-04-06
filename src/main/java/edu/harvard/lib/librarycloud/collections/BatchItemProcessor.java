package edu.harvard.lib.librarycloud.collections;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.StreamingOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import edu.harvard.lib.librarycloud.collections.CollectionsWorkflow;
import edu.harvard.lib.librarycloud.collections.dao.*;
import edu.harvard.lib.librarycloud.collections.model.*;


public class BatchItemProcessor {
    Logger log = LogManager.getLogger(BatchItemProcessor.class);

    @Autowired
    private CollectionDAO collectionDao;

    @Autowired
    private CollectionsWorkflow collectionsWorkflow;

    public boolean addBatch(Integer id, InputStream uploadedItemList) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uploadedItemList));
        boolean result = false;

        List<Item> items = new ArrayList<Item>();

        String line = "";

        while(line != null) {
            line = bufferedReader.readLine();

            if (line != null) {
                Item i = new Item();
                i.setItemId(line);
                items.add(i);
            }
        }

        result = collectionDao.addToCollection(id, items);

        if (result) {
            for (Item item : items) {
                // SQS failures should probably be logged
                // and available to a repair tool that can
                // retry them...or some other solution
                try {
                    collectionsWorkflow.notify(item.getItemId());
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}

package edu.harvard.lib.librarycloud.collections;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import edu.harvard.lib.librarycloud.collections.dao.CollectionDAO;
import edu.harvard.lib.librarycloud.collections.model.*;

public class CollectionsWorkflow {
    Logger log = Logger.getLogger(CollectionsWorkflow.class); 

    @Autowired
    private CollectionDAO collectionDao;

    /* Format for a LibraryCloud message */
    private String UPDATE_TEMPLATE = 
    	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<lib_comm_message>" + 
	    	"<command>UPDATE</command>" + 
      		"<payload>" + 
	        	"<data>%s</data>" + 
	    	"</payload>" + 
		"</lib_comm_message>";

	/**
	 * Post a message to the ENVIRONMENT-update-public SQS queue with the 
	 * updated collection information for an item
	 * @param  external_item_id ID of the item to be updated
	 * @throws Exception        
	 */
	public void notify(String external_item_id) throws Exception {

		log.error(external_item_id);
		Item item = collectionDao.getItem(external_item_id);
		log.error(item);
		String s = StringEscapeUtils.escapeXml(marshalItem(item));
		String message = String.format(UPDATE_TEMPLATE,s);

        AmazonSQSAsyncClient sqs = new AmazonSQSAsyncClient();
        CreateQueueResult createQueueResult = sqs.createQueue(Config.getInstance().SQS_ENVIRONMENT + "-update-public");
        SendMessageResult sendMessageResult = sqs.sendMessage(createQueueResult.getQueueUrl(), message);
        log.error(message);
	}

	public void notify(Collection c)  throws Exception {
		List<Item> items = collectionDao.getItems(c);
		for (Item item : items) {
			notify(item.getItemId());
		}
	}

	/**
	 * Marshall a Collections Item with associated Collections to XML
	 * @param  item      Item to be marshalled
	 * @return           XML representation of the item
	 * @throws Exception 
	 */
	private String marshalItem(Item item) throws Exception {

		StringWriter sw = new StringWriter();
		Class[] classes = { Item.class, Collection.class };
		JAXBContext jaxbContext = JAXBContext.newInstance(classes);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(item, sw);
		return sw.toString();
	}

}
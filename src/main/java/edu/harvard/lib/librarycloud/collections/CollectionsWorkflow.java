package edu.harvard.lib.librarycloud.collections;

import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import edu.harvard.lib.librarycloud.collections.dao.CollectionDAO;
import edu.harvard.lib.librarycloud.collections.model.*;

public class CollectionsWorkflow {
  Logger log = LogManager.getLogger(CollectionsWorkflow.class);

  @Autowired
  private CollectionDAO collectionDao;

  @Autowired
  private AmazonSQSAsync sqsClient;

  private Marshaller marshaller;

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
    log.debug("Notify SQS: "+external_item_id);
    Config config = Config.getInstance();
    Item item = collectionDao.getItem(external_item_id);

    String s = StringEscapeUtils.escapeXml(marshalItem(item));
    String message = String.format(UPDATE_TEMPLATE,s);
    // 20190115 added "librarycloud-" prefix
    CreateQueueResult createQueueResult = sqsClient.createQueue("librarycloud-" + config.SQS_ENVIRONMENT + "-update-public");
    SendMessageResult sendMessageResult = sqsClient.sendMessage(createQueueResult.getQueueUrl(), message);
    log.debug(sendMessageResult);
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
    if (marshaller == null) {
      Class[] classes = { Item.class, Collection.class };
      JAXBContext jaxbContext = JAXBContext.newInstance(classes);
      marshaller = jaxbContext.createMarshaller();
    }
    marshaller.marshal(item, sw);
    return sw.toString();
  }

}

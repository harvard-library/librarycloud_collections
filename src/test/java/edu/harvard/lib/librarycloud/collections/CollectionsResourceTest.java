package edu.harvard.lib.librarycloud.collections.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.BeforeClass;
import org.junit.Test;
import edu.harvard.lib.librarycloud.collections.CollectionsAPI;

/**
 * Unit tests for Collections API. Ideally this would use something like
 * JerseyTest, but the configuration required to get that working was 
 * getting complex, so using a simpler approach for now.
 */
public class CollectionsResourceTest  {

	private static String baseURL;

    private WebTarget getTarget() {
        return ClientBuilder.newBuilder().build().target(baseURL);
    }

	@BeforeClass
	public static void setUpClient() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("src/test/resources/test.properties"));
		} catch (Exception e) {
			fail("Couldn't load project configuration!");
		} 
		baseURL = props.getProperty("base_test_url");
	}

	// @Test
	public void testGetAllCollections() {
		WebTarget target = this.getTarget();
		String result = target.path("v2/collections").request().get(String.class);
		assertEquals("Request for all collections!!!!", result);
	}


}
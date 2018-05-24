package edu.harvard.lib.librarycloud.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import com.owlike.genson.ext.jaxrs.GensonJsonConverter;


public class WebAppConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        classes.add(CollectionsAPI.class);
        classes.add(GensonJsonConverter.class);

        return classes;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);
        return props;
    }
}

package edu.harvard.lib.librarycloud.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import javax.annotation.Priority;
import java.security.*;
import javax.ws.rs.ext.Provider;

import edu.harvard.lib.librarycloud.collections.dao.*;
import edu.harvard.lib.librarycloud.collections.model.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.lib.librarycloud.collections.Config;

@Provider
public class RequestLogging implements ContainerRequestFilter {

    Config config = Config.getInstance();

    public static class CachingInputStream extends BufferedInputStream {
        public CachingInputStream(InputStream source) {
            super(new PostCloseProtection(source));
            super.mark(Integer.MAX_VALUE);
        }

        @Override
        public synchronized void close() throws IOException {
            if (!((PostCloseProtection) in).decoratedClosed) {
                in.close();
            }
            super.reset();
        }

        private static class PostCloseProtection extends InputStream {
            private volatile boolean decoratedClosed = false;
            private final InputStream source;

            public PostCloseProtection(InputStream source) {
                this.source = source;
            }

            @Override
            public int read() throws IOException {
                return decoratedClosed ? -1 : source.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return decoratedClosed ? -1 : source.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return decoratedClosed ? -1 : source.read(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return decoratedClosed ? 0 : source.skip(n);
            }

            @Override
            public int available() throws IOException {
                return source.available();
            }

            @Override
            public void close() throws IOException {
                decoratedClosed = true;
                source.close();
            }

            @Override
            public void mark(int readLimit) {
                source.mark(readLimit);
            }

            @Override
            public void reset() throws IOException {
                source.reset();
            }

            @Override
            public boolean markSupported() {
                return source.markSupported();
            }
        }
    }


    Logger log = Logger.getLogger(RequestLogging.class);

    private static final String APIKEYHEADER = "X-LibraryCloud-API-Key";

    public RequestLogging(){
        log.debug("Starting Request Logging");
    }

    private void _filter(ContainerRequestContext requestContext)
        throws IOException {

        String requestMethod = requestContext.getMethod();
        UriInfo uriInfo = requestContext.getUriInfo();
        log.debug("API KEY: " + requestContext.getHeaderString(APIKEYHEADER));
        log.debug(requestMethod + " - " + uriInfo.getPath(true));
        if(requestContext.hasEntity()) {
            CachingInputStream stream = new CachingInputStream(requestContext.getEntityStream());
            String json = IOUtils.toString(stream, "UTF-8");
            stream.close();
            requestContext.setEntityStream(stream);

            log.debug(json);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        if (config.REQUEST_LOGGING) {
            _filter(requestContext);
        }
    }

}

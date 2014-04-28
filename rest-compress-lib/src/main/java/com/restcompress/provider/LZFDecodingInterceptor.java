package com.restcompress.provider;


import com.ning.compress.lzf.LZFInputStream;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interceptor to decompress HTTP message bodies with LZF, if the content encoding is LZF, before doing deserializtion
 * Works on both Server (decompress POST/PUT request bodies), and Client (response bodies)
 *
 * Based on the GZIPDecodingInterceptor:
 * <a href="https://github.com/resteasy/Resteasy/blob/Branch_2_3_7/resteasy-jaxrs/src/main/java/org/jboss/resteasy/plugins/interceptors/encoding/GZIPDecodingInterceptor.java">Source</a>
 * Modified by Sam Van Oort to accomodate the LZF format
 */
@Provider
@ServerInterceptor
@ClientInterceptor
@DecoderPrecedence
public class LZFDecodingInterceptor implements MessageBodyReaderInterceptor {

    /**
     * Check if content encoding is LZF.
     * If encoding is LZF, wrap the InputStream for that message in LZFInputStream to decode it
     * @param context Context for HTTP request/response
     * @return context.proceed()
     * @throws IOException
     * @throws WebApplicationException
     */
    public Object read(MessageBodyReaderContext context) throws IOException, WebApplicationException {

        Object encoding = context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (encoding != null && encoding.toString().equalsIgnoreCase("lzf")) {
            InputStream old = context.getInputStream();
            LZFInputStream is = new LZFInputStream(old);
            context.setInputStream(is);
            try {
                return context.proceed();
            } finally{
                context.setInputStream(old);
            }
        } else {
            return context.proceed();
        }
    }
}

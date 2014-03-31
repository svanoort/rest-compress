package org.restcompress.provider;
import com.ning.compress.lzf.LZFOutputStream;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.annotations.interception.EncoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;
import org.jboss.resteasy.util.CommitHeaderOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Encode messages in LZF if content-encoding is set to LZF (client and server)
 * Content Encoding is set by the ContentEncoding annotation in the @LZF annotation
 *
 * Based on the GZIPEncodingInterceptor
 * <a href="https://github.com/resteasy/Resteasy/blob/Branch_2_3_7/resteasy-jaxrs/src/main/java/org/jboss/resteasy/plugins/interceptors/encoding/GZIPEncodingInterceptor.java">Source</a>
 * Modified for LZF use
 */
@Provider
@ServerInterceptor
@ClientInterceptor
@EncoderPrecedence
public class LZFEncodingInterceptor implements MessageBodyWriterInterceptor
{

    /** Provides committed LZF output, which does not compress headers */
    private static class CommittedLZFOutputStream extends CommitHeaderOutputStream {
        protected CommittedLZFOutputStream(OutputStream delegate, CommitCallback headers) {
            super(delegate, headers);
        }

        protected LZFOutputStream lzf;

        public LZFOutputStream getLzf() {
            return lzf;
        }

        @Override
        public void commit() {
            if (isHeadersCommitted) return;
            isHeadersCommitted = true;

            // constructor writes to underlying OutputStream causing headers to be written.
            // so we swap compressed OutputStream in when we are ready to write.
            lzf = new LZFOutputStream(delegate);
            delegate = lzf;
        }

    }

    /**
     * Grab the outgoing message, if encoding is set to LZF, then wrap the OutputStream in an LZF OutputStream
     *   before sending it on its merry way, compressing all the time.
     *
     * Note: strips out the content-length header because the compression changes that unpredictably
     * @param context
     * @throws IOException
     * @throws WebApplicationException
     */
    public void write(MessageBodyWriterContext context) throws IOException, WebApplicationException {
        Object encoding = context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);

        if (encoding != null && encoding.toString().equalsIgnoreCase("lzf")) {
            OutputStream old = context.getOutputStream();
            // constructor writes to underlying OS causing headers to be written.
            CommittedLZFOutputStream lzfOutputStream = new CommittedLZFOutputStream(old, null);

            // Any content length set will be obsolete
            context.getHeaders().remove("Content-Length");

            context.setOutputStream(lzfOutputStream);
            try {
                context.proceed();
            } finally {
                if (lzfOutputStream.getLzf() != null) lzfOutputStream.getLzf().flush();
                context.setOutputStream(old);
            }
            return;
        } else {
            context.proceed();
        }
    }
}

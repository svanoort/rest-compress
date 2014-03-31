package org.restcompress.provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

/**
 * Client interceptor to set HTTP headers to advertise that REST client can accept LZF encoding
 */
@Provider
@ClientInterceptor
@HeaderDecoratorPrecedence
public class AcceptLZFClientInterceptor {

    /**
     * Set headers to advertise ability to accept LZF compression
     * Check if client response has HTTP header "Accept-Encoding" set:
     *    - If none, set header to "lzf", if has a header, add lzf to the list
     * @param ctx Client execution context
     * @return Response with headers appended
     * @throws Exception
     */
    public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
        String encoding = ctx.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING);
        if (encoding == null) {
            ctx.getRequest().header(HttpHeaders.ACCEPT_ENCODING, "lzf");
        } else {
            if (!encoding.contains("lzf")) {
                encoding += ", lzf";
                ctx.getRequest().header(HttpHeaders.ACCEPT_ENCODING, encoding);
            }
        }
        return ctx.proceed();
    }
}

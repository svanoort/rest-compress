package org.restcompress.provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
@ClientInterceptor
@HeaderDecoratorPrecedence
public class AcceptLZFClientInterceptor {

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

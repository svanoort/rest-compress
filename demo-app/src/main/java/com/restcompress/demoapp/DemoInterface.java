package com.restcompress.demoapp;

import com.restcompress.demoapp.model.FancyRestObject;
import com.restcompress.demoapp.model.KeyValue;
import org.jboss.resteasy.annotations.GZIP;
import com.restcompress.provider.LZF;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Shared interface used in server + clients
 * @author Sam Van Oort
 */
public interface DemoInterface {
    @GET
    @LZF
    @Path("/ping")
    /** Returns a simple text "pong" back */
    public String getPing();

    @GET
    @Path("/object")
    @LZF
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    /** Returns a sample serialized object */
    public KeyValue getObject();

    @GET
    @Path("/cached/{size}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    /** Return a random binary object, caching the results for reuse */
    public byte[] getCachedBinary(@PathParam("size") int number);

    @GET
    @Path("/complex")
    @LZF
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    /** Return new FancyObject with 10 randomized values */
    public FancyRestObject getComplexObject();

    @GET
    @Path("/complex/{size}")
    @LZF
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    /** Return new FancyObject with ${size} randomized values */
    public FancyRestObject getComplexObjectSized(@PathParam("size") int number);

    @GET
    @Path("/complex/{size}/json")
    @LZF
    /** Return new FancyObject with ${size} randomized values, formatted to JSON string*/
    public String getComplexObjectSizedJson(@PathParam("size") int number);

    @POST
    @LZF
    @Path("/lzf_uncompress")
    @Consumes("*/*")
    public byte[] decompress(InputStream strm);



    @GET
    @Path("/complex/{size}/serialized")
    @LZF
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public FancyRestObject getComplexObjectSizedMultiserializedJson(@PathParam("size") int number);

    @GET
    @LZF
    @Path("/static")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public FancyRestObject getStaticObject();

    @GET
    @GZIP
    @Path("/static/gzip")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public FancyRestObject getStaticObjectGzip();
}

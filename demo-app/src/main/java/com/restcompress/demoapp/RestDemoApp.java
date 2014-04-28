package com.restcompress.demoapp;

import com.ning.compress.lzf.LZFInputStream;
import com.restcompress.demoapp.model.FancyRestObject;
import com.restcompress.demoapp.model.KeyValue;
import com.restcompress.demoapp.model.RestMapObject;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ProxyFactory;

import javax.ws.rs.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Demo App to act as a proof of concept for LZF compression of JSON/XML outputs
 * With the jboss-web.xml, context-root for rest is /rest
 * To access, localhost:8080/rest/methodPath
 *
 * - Includes methods that return simple String (JSON formatted or otherwise) like project LightBlue (getComplexObjectSizedJson)
 * - Also has traditional return value of Serialized POJOs (like a more classic java REST service) - getComplexObjectSized
 * @author Sam Van Oort
 */
@Path("/")
public class RestDemoApp implements DemoInterface {

    static final FancyRestObject fancy = getRandomObject(10000);


    @Override
    /** Returns a simple text "pong" back */
    public String getPing() {
        return "pong";
    }

    @Override
    /** Returns a sample serialized object */
    public KeyValue getObject() {
        KeyValue obj = new KeyValue();
        obj.setKey("MyKey");
        obj.setValue("MyValue");
        return obj;
    }

    @Override
    /** Return new FancyObject with 10 randomized values */
    public FancyRestObject getComplexObject() {
        return getRandomObject(10);
    }

    @Override
    /** Return new FancyObject with ${size} randomized values */
    public FancyRestObject getComplexObjectSized(@PathParam("size") int number) {
        return getRandomObject(number);
    }

    @Override
    /** Return new FancyObject with ${size} randomized values, formatted to JSON string*/
    public String getComplexObjectSizedJson(@PathParam("size") int number) {
        return objectToJsonString(getRandomObject(number));
    }

    @Override
    public FancyRestObject getStaticObject(){
//          return getRandomObject(10000,1);
        return fancy;
    }

    @Override
    public FancyRestObject getStaticObjectGzip(){
//        return getRandomObject(10000,1);
        return fancy;
    }

    @GET
    @Path("/test/show")
    public String testShowResults() {
        DemoInterface proxy = ProxyFactory.create(DemoInterface.class, "http://localhost:8080/rest"); //Interface-based proxy
        String fancyOut = objectToJsonString(proxy.getStaticObject());
        return fancyOut;
    }


    /**
     * Test that REST clients are correctly working
     * @return
     */
    @GET
    @Path("/test")
    public String proxyTest() {
        StringBuilder retVal = new StringBuilder();
        DemoInterface proxy = ProxyFactory.create(DemoInterface.class, "http://localhost:8080/rest"); //Interface-based proxy

        String fancyOut = objectToJsonString(proxy.getStaticObject());
        if (fancyOut.equals(objectToJsonString(fancy))) {
            retVal.append("Interface-based client matches on LZF decompress. \n");
        } else {
            retVal.append("ERROR!  Interface-based DOES NOT match on LZF decompress. \n");
        }


        fancyOut = objectToJsonString(proxy.getStaticObjectGzip());
        if (fancyOut.equals(objectToJsonString(fancy))) {
            retVal.append("Interface-based client matches on GZIP decompress. \n");
        } else {
            retVal.append("ERROR!  Interface-based DOES NOT match on GZIP decompress. \n");
        }

        return retVal.toString();
    }


    /**
     * Attempts to decompress an LZF stream
     * @param strm
     * @return
     */
    @Override
    public byte[] decompress(InputStream strm) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            LZFInputStream in = new LZFInputStream(strm);
            byte[] buffer = new byte[8192];
            int len = 0;

            //Decode and copy to output
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer,0,len);
            }
            return baos.toByteArray();

        } catch (IOException ioe) {
            throw new RuntimeException("IOException reading input stream",ioe);
        }
    }


    /** Mimicks EJB-REST services
     * EJB generates object, it is serialized and deserialized over RMI to output
     * then converted to JSON/XML for REST by RESTEasy*/
    @Override
    public FancyRestObject getComplexObjectSizedMultiserializedJson(@PathParam("size") int number) {
        FancyRestObject fancy = getRandomObject(number);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(0);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(fancy);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            fancy = null;
            oos.close();
            baos = null;
            oos = null;
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            fancy = (FancyRestObject)(in.readObject());


        } catch (IOException ioException) {
            throw new RuntimeException("Failed to serialize object",ioException);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Failed to deserialize object",cnfe);
        }
        return fancy;
    }

    String objectToJsonString(Serializable object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /** Gets a random date in the last 100 years*/
    public static GregorianCalendar getRandomDate(long seed) {
        GregorianCalendar gc = new GregorianCalendar();
        Random rand = new Random(seed);
        gc.set(GregorianCalendar.YEAR,1900+rand.nextInt(120)); //Between 1900 & 2020
        gc.set(GregorianCalendar.DAY_OF_YEAR,rand.nextInt(364)+1); //Day of year
        return gc;
    }

    /**
     * Used in compression testing, returns a number of fairly complex random entries
     * @return Simple object for use in compression testing
     */
    public static FancyRestObject getRandomObject(int entries) {
        return getRandomObject(entries,System.currentTimeMillis());
    }

    public static String getRandomAlphabetic(int length, long seed) {
        Random r = new Random(seed);
        int newLength = r.nextInt(length)+1; //Random length between 1 and length characters long
        char[] characters = new char[newLength];
        for (int i=0; i<newLength; i++) {
            characters[i]= (char)(r.nextInt(26)+97); //corresponds to lowercase ASCII characters
        }
        return new String(characters);
    }

    public static FancyRestObject getRandomObject(int entries, long seed) {
        entries = Math.abs(entries);

        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); //Date
        ArrayList<RestMapObject> mapList = new ArrayList<RestMapObject>(entries);
        Random rand = new Random(seed);

        for(int i=0; i<entries; i++) {
            //Create a random Map object with id,date,login,boolean,someId fields
            RestMapObject map = new RestMapObject();

            map.setValue("id",Integer.toString(rand.nextInt(100000)));
            GregorianCalendar cal = getRandomDate(seed);
            map.setValue("date",dateFormat.format(cal.getTime()));
            map.setValue("login", getRandomAlphabetic(16,seed));
            map.setValue("boolean",Boolean.toString(rand.nextBoolean()));
            map.setValue("someId",Integer.toString(rand.nextInt(100000)));

            mapList.add(map);
        }

        FancyRestObject fancy = new FancyRestObject();
        fancy.setMaps(mapList);
        return fancy;
    }
}

package org.restcompress.demoapp.model;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Replaces a FancyRestObject with smarter packing of a collection
 * Replaces a List\<RestMapObject\> which is basically String key-value sets
 * The key-value sets are converted to:
 * Array of String header names
 * ArrayList of String[] (values for these objects)
 *
 * This whole shebang should (de)serialize and convert to XML/JSON very efficiently
 *
 * //TODO Finished Packed List implementation
 * //TODO Explore what impacts packed fancy rest object has on (de)serialization & XML/JSON performance
 */
@XmlRootElement
public class PackedFancyRestObject implements Serializable {

    /** Packed Values */
    ArrayList<String[]> values = new ArrayList<String[]>();


}

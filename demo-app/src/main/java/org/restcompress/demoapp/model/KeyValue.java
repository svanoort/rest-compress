package org.restcompress.demoapp.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Basic Serializable sample REST Object
 * Mimicks a key-value pair
 */
@XmlRootElement
public class KeyValue implements Serializable {
    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;

        if (key == null) {
            throw new IllegalArgumentException("Null key specified!");
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public KeyValue() {
    }

    public KeyValue(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Null key specified!");
        }
        this.key = key;
        this.value = value;
    }
}

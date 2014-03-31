package org.restcompress.demoapp.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pseudo map object for REST interfaces, used in benchmarking
 */
@XmlRootElement
public class RestMapObject implements Serializable {
    private List<KeyValue> map = new ArrayList<KeyValue>();

    /** Mimicks a key-value map */
    public List<KeyValue> getMap() {
        return map;
    }

    public void setMap(List<KeyValue> map) {
        this.map = map;
    }

    KeyValue getKV(String key) {
        if (key != null && !key.isEmpty()) {
            for (KeyValue kv : map) {
                if (key.equals(kv.getKey())) {
                    return kv;
                }
            }
        }
        return null;
    }

    /** Get the value for the key */
    public String getValue(String key) {
        KeyValue kv = getKV(key);
        return kv == null ? null : kv.getValue();
    }

    /** Null value is totally legal! */
    public void setValue(String key, String value) {
        KeyValue kv = getKV(key);
        if (kv != null) {
            kv.setValue(value);
        } else if (key != null) {
            map.add(new KeyValue(key,value));
        }
    }

    /** Remove mappings for given key */
    public void removeKey(String key) {
        if (key != null && !key.isEmpty()) {
            for (int i=0; i<map.size(); i++) {
                KeyValue kv = map.get(i);
                if (key.equals(kv.getKey())) {
                    map.remove(i);
                    return;
                }
            }
        }
    }
}

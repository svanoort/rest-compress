package org.restcompress.demoapp.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fancy Serializable REST Object
 * Used for testing compression of objects
 * This can be generated with a lot of nested levels of complexity
 */
@XmlRootElement
public class FancyRestObject implements Serializable {
    List<RestMapObject> maps = new ArrayList<RestMapObject>();

    public List<RestMapObject> getMaps() {
        return maps;
    }

    public void setMaps(List<RestMapObject> maps) {
        this.maps = maps;
    }
}

package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BlueExtensionProxies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class ExtensionProxiesImpl extends BlueExtensionProxies {
    private final List<Object> values = new ArrayList<>();

    public ExtensionProxiesImpl() {
    }

    public ExtensionProxiesImpl add(Object value){
        values.add(value);
        return this;
    }

    public Iterator<Object> iterator() {
        return values.iterator();
    }
}

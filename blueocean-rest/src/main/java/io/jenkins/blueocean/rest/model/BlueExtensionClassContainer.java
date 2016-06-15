package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.hal.Link;

import java.util.Iterator;

/**
 * Container of {@link BlueExtensionClass}es
 *
 * @author Vivek Pandey
 */
public abstract class BlueExtensionClassContainer extends Container<BlueExtensionClass> implements ApiRoutable, ExtensionPoint{

    @Override
    public String getUrlName() {
        return "classes";
    }

    @Override
    public Iterator<BlueExtensionClass> iterator() {
        return null;
    }

    @Override
    public Link getLink() {
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }
}

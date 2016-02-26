package io.jenkins.blueocean.rest;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.pageable.Pageable;

/**
 * Extension point to contribute the search capability
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 * @see ApiHead#search(Query)
 */
public abstract class OmniSearch<T> implements ExtensionPoint {

    public abstract String getType();

    public abstract Pageable<T> search(Query q);

    public static ExtensionList<OmniSearch> all() {
        return ExtensionList.lookup(OmniSearch.class);
    }
}

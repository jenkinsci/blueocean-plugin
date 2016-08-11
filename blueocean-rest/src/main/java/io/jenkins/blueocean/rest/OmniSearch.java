package io.jenkins.blueocean.rest;

import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;

/**
 * Extension point to contribute the search capability
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 * @see ApiHead#search(Query)
 */
public abstract class OmniSearch<T> implements ExtensionPoint {

    public abstract String getType();

    public abstract Pageable<T> search(Query q, ItemGroup<?> root);

    public static ExtensionList<OmniSearch> all() {
        return ExtensionList.lookup(OmniSearch.class);
    }

    /**
     * Executes the given query against the given
     * item container
     */
    public static Pageable<?> query(Query query, ItemGroup<?> root) {
        for (OmniSearch os : all()) {
            if (os.getType().equals(query.type))
                return os.search(query, root);
        }
        return Pageables.empty();
    }
}

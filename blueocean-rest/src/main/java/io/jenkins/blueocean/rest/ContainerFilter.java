package io.jenkins.blueocean.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Predicate;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;

/**
 * Simple extension point to allow filtering item types by a specific key
 * This can be used anywhere a list of items are returned, and it will examine
 * the current request for a ?filter=filter1(param:value),filter2 parameter,
 * okay the last bit is a future addition once/if OmniSearch and this are consolidated
 */
public abstract class ContainerFilter implements ExtensionPoint {
    /**
     * Name to match
     */
    public abstract String getName();
    
    /**
     * Predicate to filter items
     */
    public abstract Predicate<Item> getFilter();
    
    /**
     * Filters the item list based on the current StaplerRequest
     */
    public static <T extends Item>  Collection<T> filter(Collection<T> items) {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            return items;
        }
        String itemFilter = req.getParameter("filter");
        if (itemFilter == null) {
            return items;
        }
        return filter(items, itemFilter.split(","));
    }
    
    /**
     * Filters the item list based on the supplied filter name
     */
    public static <T extends Item> Collection<T> filter(Collection<T> items, String ... filterNames) {
        if (filterNames != null) {
            Collection<T> out = new ArrayList<>();
            for (T item : items) {
                for (String filterName : filterNames) {
                    final Predicate<Item> f = getItemFilter(filterName);
                    if (f == null) {
                        throw new IllegalArgumentException("Invalid filter type specified.");
                    }
                    if (f.apply(item)) {
                        out.add(item);
                    }
                }
            }
            return out;
        }
        return items;
    }
    
    /**
     * Finds a item filter by name
     */
    public static Predicate<Item> getItemFilter(String filterName) {
        for (ContainerFilter itemFilter : ExtensionList.lookup(ContainerFilter.class)) {
            if (itemFilter.getName().equals(filterName)) {
                return itemFilter.getFilter();
            }
        }
        return null;
    }
}

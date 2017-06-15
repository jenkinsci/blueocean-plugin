package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;

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
        String[] filterNames =  filterNames();
        if(filterNames.length == 0){
            return items;
        }
        return filter(items, filterNames);
    }

    /**
     * Filters the item list based on the supplied filter name
     */
    public static <T extends Item> Collection<T> filter(Collection<T> items, String ... filterNames) {
        Predicate<Item>[] filters = getFilters(filterNames);
        Collection<T> out = new LinkedList<>();
        nextItem: for (T item : items) {
            for (Predicate<Item> filter : filters) {
                if (!filter.apply(item)) {
                    continue nextItem;
                }
            }
            out.add(item);
        }
        return out;
    }

    /**
     * Filter items based on supplied fiter and paging criteria
     */
    public static <T extends Item> Collection<T> filter(Collection<T> items, int start, int limit) {
        String[] filterNames = filterNames();

        int skipped=0;
        Predicate<Item>[] filters = getFilters(filterNames);
        Collection<T> out = new LinkedList<>();

        nextItem: for (T item : items) {
            //if collected items of size 'limit' we are done
            if(out.size() == limit){
                break;
            }
            for (Predicate<Item> filter : filters) {
                if (!filter.apply(item)) {
                    continue nextItem;
                }
            }
            //if there is need to skip, skip these items
            if(skipped++ < start){
                continue;
            }
            out.add(item);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Predicate<Item>[] getFilters(@Nonnull String...filterNames){
        Predicate<Item>[] filters = new Predicate[filterNames.length];
        if (filterNames.length > 0) {
            for (int i = 0; i < filterNames.length; i++) {
                final Predicate<Item> f = getItemFilter(filterNames[i]);
                if (f == null) {
                    throw new IllegalArgumentException("Invalid filter type specified.");
                }
                filters[i] = f;
            }
        }
        return filters;
    }

    private static String[] filterNames(){
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            return new String[0];
        }
        String itemFilter = req.getParameter("filter");
        if (itemFilter == null) {
            return new String[0];
        }
        return itemFilter.split(",");
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

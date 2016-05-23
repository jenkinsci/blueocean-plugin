package io.jenkins.blueocean.rest.model;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.util.AdaptedIterator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Containers {

    public abstract static class AbstractContainer<T extends Resource> extends Container<T> {
    }

    public static <T> Container<Resource> from(final List<T> base) {
        return new AbstractContainer<Resource>() {
            @Override
            public Resource get(String name) {
                int idx = Integer.parseInt(name);   // TODO: more graceful error check
                return new GenericResource<>(base.get(idx));
            }

            @Override
            public Iterator<Resource> iterator() {
                return iterator(0,base.size());
            }

            @Override
            public Iterator<Resource> iterator(int start, int limit) {
                return new AdaptedIterator<T,Resource>(base.subList(start,start+limit)) {
                    @Override
                    protected Resource adapt(T item) {
                        return new GenericResource<>(item);
                    }
                };
            }
        };
    }

    public static <T> Container<Resource> from(final Map<String,T> base) {

        return new AbstractContainer<Resource>() {
            @Override
            public Resource get(String name) {
                T u = base.get(name);
                if (u==null)    return null;
                return new GenericResource<>(u);
            }

            @Override
            public Iterator<Resource> iterator() {
                return new AdaptedIterator<T,Resource>(base.values()) {
                    @Override
                    protected Resource adapt(T item) {
                        return new GenericResource<>(item);
                    }
                };
            }
        };
    }


    public static <T extends Resource> Container<T> fromResourceMap(final Map<String,T> base) {
        return new AbstractContainer<T>() {
            @Override
            public T get(String name) {
                T u = base.get(name);
                if (u==null)    return null;
                return u;
            }

            @Override
            public Iterator<T> iterator() {
                return base.values().iterator();
            }
        };
    }

    /**
     *
     * @param entries
     * @param <T>
     * @return
     */
    public static <T extends Resource> Container<T> fromResourceMapEntries(final Iterable<Map.Entry<String, T>> entries) {
        return new AbstractContainer<T>() {
            @Override
            public T get(final String name) {
                Predicate<Map.Entry<String, T>> pred = new Predicate<Map.Entry<String, T>>() {
                    @Override
                    public boolean apply(@Nullable Map.Entry<String, T> input) {
                        return input.getKey().equals(name);
                    }
                };
                Map.Entry<String, T> entry =  Iterables.find(entries, pred);

                return entry == null ? null : entry.getValue();
            }

            @Override
            public Iterator<T> iterator() {
                Function<Map.Entry<String, T>, T> func = new Function<Map.Entry<String, T>, T>() {
                    @Override
                    public T apply(@Nullable Map.Entry<String, T> input) {
                        return input.getValue();
                    }
                };
                return Iterables.transform(entries, func).iterator();
            }
        };
    }
}

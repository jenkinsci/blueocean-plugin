package io.jenkins.blueocean.rest.model;

import com.google.common.collect.ImmutableMap;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.hal.Link;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Containers {

    public abstract static class AbstractContainer<T extends Resource> extends Container<T> {
    }


    public static <T extends Resource> Container<T> fromResource(final Link self, final List<T> base) {
        return new AbstractContainer<T>() {
            @Override
            public Link getLink() {
                return self;
            }

            @Override
            public T get(String name) {
                int idx = Integer.parseInt(name);   // TODO: more graceful error check
                return base.get(idx);
            }

            @Override
            public Iterator<T> iterator() {
                return iterator(0,base.size());
            }

            @Override
            public Iterator<T> iterator(int start, int limit) {
                if (start >= base.size()) {
                    return Collections.<T>emptyList().iterator();
                }
                int end = start+limit;
                if (end > base.size()) {
                    end = base.size();
                }
                return base.subList(start,end).iterator();
            }
        };
    }


    public static <T> Container<Resource> from(final Link self, final List<T> base) {
        return new AbstractContainer<Resource>() {
            @Override
            public Link getLink() {
                return self;
            }

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

    public static <T> Container<Resource> from(final Link self, final Map<String,T> base) {

        return new AbstractContainer<Resource>() {
            @Override
            public Link getLink() {
                return self;
            }

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

    public static <T extends Resource> Container<T> fromResourceMap(final Link self, final Map<String,T> base) {
        return new AbstractContainer<T>() {
            @Override
            public Link getLink() {
                return self;
            }

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

    public static <T extends Resource> Container<T> empty(Link self) {
        return Containers.fromResourceMap(self, ImmutableMap.<String, T>of());
    }
}

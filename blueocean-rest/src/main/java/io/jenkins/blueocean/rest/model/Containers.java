package io.jenkins.blueocean.rest.model;

import hudson.util.AdaptedIterator;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Containers {
    @ExportedBean
    public abstract static class AbstractContainer extends Container<Resource>{
    }
    public static <T> Container<Resource> from(final List<T> base) {

        return new AbstractContainer() {
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

            @Override
            public int size() {
                return base.size();
            }


        };
    }

    public static <T> Container<Resource> from(final Map<String,T> base) {

        return new AbstractContainer() {
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

            @Override
            public int size() {
                return base.entrySet().size();
            }
        };
    }
}

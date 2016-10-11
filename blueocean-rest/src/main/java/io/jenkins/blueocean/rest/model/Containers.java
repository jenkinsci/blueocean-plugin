package io.jenkins.blueocean.rest.model;

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
    
    /**
     * Combines multiple {@link Iterable} to treat them as a single one
     *
     * @param <T> iterable type
     */
    public static class CombinedIterable<T> implements Iterable<T> {
        private final Iterable<? extends T>[] iterables;
        
        @SafeVarargs
        public CombinedIterable(Iterable<? extends T> ... iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<T> iterator() {
            return iterator(0, -1); // no limit
        }

        public Iterator<T> iterator(final int start, final int limit) {
            if (iterables.length == 0) {
                return Collections.emptyIterator();
            }
            return new Iterator<T>() {
                // We might want to limit/default the max
                final int end = limit < 0 ? -1 : (start + limit);
                Iterator<? extends T> iterator;
                private T next;
                int index = 0;
                int iterableIndex = -1;
                {
                    // primer
                    nextIterator();
                    moveNext();
                }
                private void nextIterator() {
                    iterableIndex++;
                    if (iterableIndex < iterables.length) {
                        iterator = iterables[iterableIndex].iterator();
                    } else {
                        iterator = null;
                    }
                }
                private void moveNext() {
                    // Skip until we hit our starting index
                    while (index < start) {
                        if (iterator == null) {
                            return;
                        }
                        if (iterator.hasNext()) {
                            iterator.next();
                            index++;
                        } else {
                            nextIterator();
                        }
                    }

                    // keep adding until we hit the limit or run out of items
                    if (index < end || end < 0) {
                        while (iterator != null && !iterator.hasNext()) {
                            nextIterator();
                            if (iterator == null) {
                                return;
                            }
                        }

                        next = iterator.next();
                        index++;
                    } else {
                        iterableIndex = iterables.length;
                        iterator = null;
                    }
                }

                @Override
                public boolean hasNext() {
                    return iterator != null;
                }

                @Override
                public T next() {
                    try {
                        return next;
                    } finally {
                        moveNext();
                    }
                }

                @Override
                public void remove() {
                    throw new RuntimeException("remove() not implemented.");
                }
            };
        }
    }

    /**
     * Simple method to provide a combination of {@link Iterable} with the ability to implement a specific
     * {@link #get(String)} method in a subclass. This will also iterate as efficiently as possible
     * through the provided iterators to provide paged data - e.g. requesting page 0-9 will only
     * iterate through the first 10 items, in the order of the provided {@link Iterable}
     */
    public abstract static class IterableContainer<T extends Resource> extends Container<T> {
        private final CombinedIterable<T> iterable;
        private final Link link;

        @SafeVarargs
        public IterableContainer(Link link, Iterable<? extends T> ... iterables) {
            this.link = link;
            this.iterable = new CombinedIterable<T>(iterables);
        }

        @Override
        public Link getLink() {
            return link;
        }

        @Override
        public Iterator<T> iterator() {
            return iterator(0, -1); // get 'everything'
        }

        @Override
        public Iterator<T> iterator(final int start, final int limit) {
            return iterable.iterator(start, limit);
        }
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

    /**
     * Allows generic iteration and search over iterators
     * @param self
     * @return
     */
    @SafeVarargs
    public static <T extends Resource> Container<T> fromIterables(final Link self, final Iterable<T> ... iterables) {
        return new IterableContainer<T>(self, iterables) {
            @Override
            public T get(String index) {
                int idx;
                try {
                    idx = Integer.parseInt(index);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid index, expected a number");
                }
                Iterator<T> i = iterator(idx, 1);
                if (i.hasNext()) {
                    return i.next();
                }
                return null; // TODO throw a not found?
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
}

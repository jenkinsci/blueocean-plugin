package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Pageables {
    private Pageables() {} // no instantiation

    /**
     * Returns an empty {@link Pageable}
     * @param <T> type of Pageable item
     * @return empty pageable collection
     */
    public static <T> Pageable<T> empty() {
        return wrap(Collections.<T>emptyList());
    }

    /**
     * Poorman's {@link Pageable} implementation that does
     * skipping by simply fast-forwarding {@link Iterator}
     *
     * @param base base collection
     * @param start starting index requested from collection
     * @param limit max number of item requested in the page
     * @param <T> type of Pageable item
     * @return iterator with starting index==start and size &lt; limit
     */
    public static <T> Iterator<T> slice(Iterator<T> base, int start, int limit) {
        // fast-forward
        int skipped = Iterators.skip(base,start);
        if (skipped < start){ //already at the end, nothing to return
                Iterators.emptyIterator();
        }
        return Iterators.limit(base, limit);
    }

    /**
     * Wraps {@link Iterable} into a {@link Pageable}
     *
     * @param base collection to be wrapped in to Pageable
     * @return Pageable collection
     */
    public static <T> Pageable<T> wrap(final Iterable<T> base) {
        return new Pageable<T>() {
            @Override
            public Iterator<T> iterator(int start, int limit) {
                return slice(iterator(),start,limit);
            }

            @Override
            public Iterator<T> iterator() {
                return base.iterator();
            }
        };
    }
}

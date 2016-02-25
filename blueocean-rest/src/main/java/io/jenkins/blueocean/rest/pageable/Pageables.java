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
     */
    public static <T> Pageable<T> empty() {
        return wrap(Collections.<T>emptyList());
    }

    /**
     * Poorman's {@link Pageable} implementation that does
     * skipping by simply fast-forwarding {@link Iterator}
     */
    public static <T> Iterator<T> slice(Iterator<T> base, int start, int limit) {
        // fast-forward
        if (Iterators.skip(base,start)!=start)
            throw new ArrayIndexOutOfBoundsException();
        return Iterators.limit(base, limit);
    }

    /**
     * Wraps {@link Iterable} into a {@link Pageable}
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

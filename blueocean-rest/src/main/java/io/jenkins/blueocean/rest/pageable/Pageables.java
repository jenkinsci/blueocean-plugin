package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;

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
        return new Pageable<T>() {
            @Override
            public Iterator<T> iterator(int start, int limit) {
                if (start!=0 || limit!=0)
                    throw new ArrayIndexOutOfBoundsException();
                return iterator();
            }

            @Override
            public Iterator<T> iterator() {
                return Iterators.<T>emptyIterator();
            }
        };
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
}

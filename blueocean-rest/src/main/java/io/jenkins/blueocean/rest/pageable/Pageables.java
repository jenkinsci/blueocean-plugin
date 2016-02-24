package io.jenkins.blueocean.rest.pageable;

import hudson.util.Iterators;

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
                return Iterators.<T>empty();
            }
        };
    }
}

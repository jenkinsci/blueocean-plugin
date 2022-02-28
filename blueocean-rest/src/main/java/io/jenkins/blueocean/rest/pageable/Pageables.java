package io.jenkins.blueocean.rest.pageable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Pageables {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pageables.class);

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
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(base, Spliterator.SORTED), false)
            .skip(start)
            .limit(limit)
            .iterator();
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

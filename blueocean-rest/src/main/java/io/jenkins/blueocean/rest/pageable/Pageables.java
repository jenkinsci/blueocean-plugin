package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
        if (Iterators.skip(base,start)!=start)
            throw new ArrayIndexOutOfBoundsException();
        return Iterators.limit(base, limit);
    }

    /**
     * Combines iterators and enforces start and limit. Combination occurs in the order of iterators in the array.
     *
     * @param start starting index
     * @param limit max items requested. If -1, then its considered no limit
     * @param iterators iterators
     * @param <T> item type
     * @return gives combined iterator
     */
    @SafeVarargs
    public static <T> Iterator<T> combine(int start, int limit, Iterator<T> ...iterators){
        int count = 0;
        List<T> items = new ArrayList<>();
        for(Iterator<T> iterator:iterators){
            int skipped = Iterators.skip(iterator,start);
            if(skipped > 0){
                start = start-skipped;
                continue;
            }
            if((count < limit || limit ==-1) && iterator.hasNext()){
                items.add(iterator.next());
                count++;
            }
        }
        return items.iterator();
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

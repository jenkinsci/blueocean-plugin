package io.jenkins.blueocean.commons.stapler.export;

import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Specifies the range in a collection.
 *
 * @author Kohsuke Kawaguchi
 */
public class Range {
    public final int min;
    public final int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public <T> List<T> apply(T[] a) {
        return apply(Arrays.asList(a));
    }

    public <T> List<T> apply(List<T> s) {
        if (max<s.size())   s = s.subList(0,max);
        if (min>0)          s = s.subList(min,s.size());
        return s;
    }

    public <T> Iterable<T> apply(final Collection<T> s) {
        return apply((Iterable<T>)s);
    }

    public <T> Iterable<T> apply(final Iterable<T> s) {
        if (s instanceof List) {
            return apply((List<T>) s);
        } else {
            return new Iterable<T>() {
                public Iterator<T> iterator() {
                    Iterator<T> itr = s.iterator();
                    itr = Iterators.limit(itr,max);
                    if (min>0)
                        advance(itr,min);
                    return itr;
                }
            };
        }
    }

    public static int advance(Iterator<?> iterator, int numberToAdvance) {
        int i;
        for (i = 0; i < numberToAdvance && iterator.hasNext(); i++) {
            iterator.next();
        }
        return i;
    }

    /**
     * Range that includes natural numbers.
     */
    public static final Range ALL = new Range(0,Integer.MAX_VALUE);
}

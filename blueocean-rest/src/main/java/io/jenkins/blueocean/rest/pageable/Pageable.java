package io.jenkins.blueocean.rest.pageable;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Pageable<T> extends Iterable<T> {
    public static final int DEFAULT_LIMIT=100;

    default public int defaultLimit() {
      return DEFAULT_LIMIT;
    }

    /**
     * Returns a iterator that visits a subset, which is used by the HTTP layer to do the pagenation.
     * @param start starting index requested from collection
     * @param limit max number of item requested in the page
     * @return filtered collection
     */
    Iterator<T> iterator(int start, int limit);
}

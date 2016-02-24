package io.jenkins.blueocean.rest.pageable;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Pageable<T> extends Iterable<T> {
    /**
     * Returns a iterator that visits a subset, which is used by the HTTP layer to do the pagenation.
     */
    Iterator<T> iterator(int start, int limit);
}

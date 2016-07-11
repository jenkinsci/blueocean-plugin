package io.jenkins.blueocean.rest.hal;

/**
 *
 * Resolves a {@link Link} for a given model object
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class LinkResolver {

    /**
     *
     * @param modelObject
     *      a model object to map to corresponding BlueOcean API model object
     *
     * @return
     *      {@link Link} to BlueOcean API model object. null if there is no BlueOcean API model object found that maps
     *      to the given model object.
     */
    public abstract Link resolve(Object modelObject);
}

package io.jenkins.blueocean.rest.hal;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 *
 * Resolves a {@link Link} for a given model object
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class LinkResolver implements ExtensionPoint{

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

    public static ExtensionList<LinkResolver> all(){
        return ExtensionList.lookup(LinkResolver.class);
    }

    public static Link resolveLink(Object modeObject){
        for(LinkResolver resolver:all()){
            Link link = resolver.resolve(modeObject);
            if(link != null){
                return link;
            }
        }
        return null;
    }
}

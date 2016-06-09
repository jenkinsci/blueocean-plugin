package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.Routable;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;

import java.util.List;

/**
 * Stapler-bound object that defines REST endpoint.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class Resource implements Routable, Reachable{
    /**
     * Returns the DTO object that gets databound to Json/XML etc. for state transfer
     *
     * @return DTO object
     */
    @WebMethod(name="") @GET
    @TreeResponse /* this annotation does the above new Api(...).doJson(...) */
    public Object getState() {
        return this;
    }

    /**
     * Links to all resources available in the context of this resource
     *
     * @return {@link Links} object
     */
    @Exported(name = "_links", visibility = 9999)
    public Links getLinks(){
        return new Links(this);
    }

    public String getUrlName(){
        return "";
    }

    /**
     * {@link Link} to this resource.
     *
     * In certain cases, such as when this URL is exposed via collection API, such as GET /pipelines, getUrl() would
     * give path to /pipelines and not /pipelines/{id}. In such cases specific implementation of the resource should
     * override this method.
     *
     * @see BluePipeline#getLink()
     */
    public Link getLink(){
        Ancestor ancestor = Stapler.getCurrentRequest().findAncestor(this.getClass());
        if(ancestor == null){
            List<Ancestor> ancestors = Stapler.getCurrentRequest().getAncestors();

            //get current ancestor
            Ancestor current = ancestors.get(ancestors.size()-1);
            String contextPath = Stapler.getCurrentRequest().getContextPath();
            String parentUrl = current.getUrl();
            if(!contextPath.isEmpty() && !contextPath.equals("/")){
                int i = parentUrl.indexOf(contextPath);
                if(i>=0 && i+contextPath.length() < parentUrl.length()){
                    parentUrl = parentUrl.substring(i+contextPath.length());
                }
            }
            if(!getUrlName().isEmpty()){
                return new Link(String.format("%s/%s",parentUrl,getUrlName()));
            }else{
                return new Link(String.format("%s/",parentUrl));
            }


        }
        return new Link(Stapler.getCurrentRequest().getPathInfo());
    }

}

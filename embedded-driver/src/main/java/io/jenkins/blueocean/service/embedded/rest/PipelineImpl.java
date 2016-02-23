package io.jenkins.blueocean.service.embedded.rest;

import hudson.XmlFile;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.sandbox.Pipeline;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl extends Pipeline {
    private final OrganizationImpl parent;

    protected PipelineImpl(OrganizationImpl parent) {
        this.parent = parent;
    }

    private boolean populated;

    private void populate() {
        if (!populated) {
            try {
                getXmlFile().unmarshal(this);
                populated = true;
            } catch (IOException e) {
                throw new IOError(e);
            }
        }
    }

    private XmlFile getXmlFile() {
//        return new XmlFile(new File(Jenkins.getInstance().getRootDir(),"organizations/"+name));
        throw new UnsupportedOperationException("This is just an example");
    }

    @Override
    @WebMethod(name="") @GET @TreeResponse
    public Object getState() {
        populate();
        return super.getState();
    }

}

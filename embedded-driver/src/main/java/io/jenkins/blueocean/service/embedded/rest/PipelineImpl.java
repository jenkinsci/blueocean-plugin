package io.jenkins.blueocean.service.embedded.rest;

import hudson.XmlFile;
import io.jenkins.blueocean.rest.sandbox.Pipeline;
import jenkins.model.Jenkins;

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
        return new XmlFile(new File(Jenkins.getInstance().getRootDir(),"organizations/"+name));
    }

    @Override
    public Object doIndex() {
        populate();
        return super.getState();
    }

}

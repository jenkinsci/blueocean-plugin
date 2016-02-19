package io.jenkins.blueocean.rest.sandbox.embedded;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.XmlFile;
import io.jenkins.blueocean.rest.sandbox.Organization;
import io.jenkins.blueocean.rest.sandbox.PipelineContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class OrganizationImpl extends Organization {
    @JsonProperty
    String iconUrl;

    @Override
    public String getIconUrl() {
        populate();
        return iconUrl;
    }

    @Override
    public PipelineContainer getPipelines() {
        return null;
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
        return super.doIndex();
    }

    @WebMethod(name="",method="POST")
    public void update(@JsonBody OrganizationImpl given) throws IOException {
        given.validate();
        getXmlFile().write(given);
    }

    private void validate() {
        if (name.length()<2)
            throw new IllegalArgumentException("Invalid name: "+name);
    }
}

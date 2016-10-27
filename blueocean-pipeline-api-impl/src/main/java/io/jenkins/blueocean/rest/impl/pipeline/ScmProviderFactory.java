package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionList;
import org.apache.tools.ant.ExtensionPoint;

/**
 * @author Vivek Pandey
 */
public abstract class ScmProviderFactory  extends ExtensionPoint{

    public abstract ScmProvider getScmProvider(String id);
    public static ExtensionList<ScmProviderFactory> all(){
        return ExtensionList.lookup(ScmProviderFactory.class);
    }
}

package io.jenkins.embryo;

import hudson.Extension;
import hudson.ExtensionComponent;
import hudson.scm.SCM;
import hudson.tools.ToolInstallation;
import hudson.widgets.Widget;
import jenkins.ExtensionFilter;
import jenkins.model.Jenkins;

/**
 * Removes unwanted functionalities from Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DescriptorFilterImpl extends ExtensionFilter {
    @Override
    public <T> boolean allows(Class<T> type, ExtensionComponent<T> component) {
        if (type==Widget.class && isFromCore(component))     return false;
        if (component.isDescriptorOf(SCM.class)) return false;
        if (component.isDescriptorOf(ToolInstallation.class)) return false;
        // TODO: lot more can be removed here

        return true;
    }

    private <T> boolean isFromCore(ExtensionComponent<T> component) {
        return component.getInstance().getClass().getClassLoader()==Jenkins.class.getClassLoader();
    }
}

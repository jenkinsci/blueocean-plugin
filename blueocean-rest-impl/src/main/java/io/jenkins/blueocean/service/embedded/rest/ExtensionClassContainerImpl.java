package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import io.jenkins.blueocean.rest.model.BlueExtensionClassContainer;

/**
 * @author Vivek Pandey
 */
@Extension
public class ExtensionClassContainerImpl extends BlueExtensionClassContainer {

    @Override
    public BlueExtensionClass get(String name) {
        try {
            Class clz = this.getClass().getClassLoader().loadClass(name);
            return new ExtensionClassImpl(this,clz);
        } catch (ClassNotFoundException e) {
            throw new ServiceException.NotFoundException(String.format("Class %s is not known", name));
        }
    }
}

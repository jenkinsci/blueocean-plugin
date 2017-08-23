package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import io.jenkins.blueocean.rest.model.BlueExtensionClassContainer;
import io.jenkins.blueocean.rest.model.BlueExtensionClassMap;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Extension
public class ExtensionClassContainerImpl extends BlueExtensionClassContainer {

    @Override
    public BlueExtensionClass get(String name) {
        return new ExtensionClassImpl(getClazz(name), this);
    }

    @Override
    public BlueExtensionClassMap getMap(@JsonBody Map<String, List<String>> request) {
        List<String> cl = request.get("q")!=null ? request.get("q") : Collections.<String>emptyList();
        return new BlueExtensionClassMapImpl(cl, ExtensionClassContainerImpl.this.getLink());
    }


    @Override
    public Link getLink() {
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }

    private static Class getClazz(String name){
        try {
            return Jenkins.getInstance().getPluginManager().uberClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static class BlueExtensionClassMapImpl extends BlueExtensionClassMap{

        private final Map<String, BlueExtensionClass> classMap = new HashMap<>();
        private final Link self;

        public BlueExtensionClassMapImpl(List<String> classList, Link self) {
            for(String c:classList){
                classMap.put(c, new ExtensionClassImpl(getClazz(c), this));
            }
            this.self = self;
        }

        @Override
        public Link getLink() {
            return self;
        }

        @Override
        public Map<String, BlueExtensionClass> getMap() {
            return classMap;
        }
    }
}

package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class ExtensionClassImpl extends BlueExtensionClass {
    private final Class baseClass;
    private final Reachable parent;

    public ExtensionClassImpl(Class baseClass, Reachable parent) {
        this.baseClass = baseClass;
        this.parent = parent;
    }

    @Override
    public Collection<String> getClasses() {
        List<String> classes = new ArrayList<>();
        if(baseClass != null) {
            collectSuperClasses(classes, baseClass);
        }
        return classes;
    }

    private void collectSuperClasses(List<String> classes, Class base){
        captureCapabilityAnnotation(classes,base);
        Class clz = base.getSuperclass();
        if(clz != null && !isBlackListed(clz.getName()) && !classes.contains(clz.getName())){
            classes.add(clz.getName());
            captureCapabilityAnnotation(classes,clz);
            collectSuperClasses(classes,clz);
        }
    }

    private void captureCapabilityAnnotation(List<String> classes, Class clz){
        Annotation annotation = clz.getAnnotation(Capability.class);
        if(annotation != null){
            Capability capability = (Capability) annotation;
            for(String c:capability.value()) {
                if(!classes.contains(c)) {
                    classes.add(c);
                }
            }
        }
    }

    private boolean isBlackListed(String clz){
        for(String s:BLACK_LISTED_CLASSES){
            if(clz.startsWith(s)){
                return true;
            }
        }
        return false;
    }

    private static final String[] BLACK_LISTED_CLASSES={"java.", "javax.", "com.sun."};


    public Link getLink() {
        return parent.getLink().rel(baseClass.getName());
    }
}

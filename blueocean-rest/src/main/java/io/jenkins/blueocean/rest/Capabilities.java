package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.rest.annotation.Capability;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Capabilities {

    public static Set<String> allCapabilities(Class base) {
        Set<String> classes = new LinkedHashSet<>();
        collectSuperClasses(classes, base, true);
        return classes;
    }

    public static Set<String> allSuperClassesAndCapabilities(Class base) {
        Set<String> classes = new LinkedHashSet<>();
        collectSuperClasses(classes, base, false);
        return classes;
    }

    private static void collectSuperClasses(Set<String> classes, Class base, boolean capabilitiesOnly){
        captureCapabilityAnnotation(classes,base);
        Class clz = base.getSuperclass();
        if(clz != null && !isBlackListed(clz.getName()) && !classes.contains(clz.getName())){
            if (!capabilitiesOnly) {
                classes.add(clz.getName());
            }
            captureCapabilityAnnotation(classes,clz);
            collectSuperClasses(classes,clz, capabilitiesOnly);
        }
    }

    private static void captureCapabilityAnnotation(Set<String> classes, Class clz){
        Annotation annotation = clz.getAnnotation(Capability.class);
        if(annotation != null){
            Capability capability = (Capability) annotation;
            Collections.addAll(classes, capability.value());
        }
    }

    private static boolean isBlackListed(String clz){
        for(String s:BLACK_LISTED_CLASSES){
            if(clz.startsWith(s)){
                return true;
            }
        }
        return false;
    }

    private static final String[] BLACK_LISTED_CLASSES={"java.", "javax.", "com.sun."};


    private Capabilities() {
    }
}

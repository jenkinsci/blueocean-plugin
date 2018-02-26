package io.jenkins.blueocean.service.embedded.util;

import hudson.model.Item;
import io.jenkins.blueocean.rest.annotation.Capability;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Helpers to query @Capability annotations server-side
 */
public class Capabilities {

    /**
     * Check if an item is annotated with a specific capability. Checks super classes even though annotation is not
     * inherited, because that echoes the behaviour when building REST responses.
     *
     * @param item a Jenkins Item / BO object
     * @param capability the string name of the capability you wish to search for
     * @return true if annotation is found
     */
    public static boolean hasCapability(Object item, String capability) {
        return item != null && capability != null && Capabilities.hasCapability(item.getClass(), capability);
    }

    private static boolean hasCapability(@Nonnull Class<?> clazz, @Nonnull String capability) {

        Class<?> searchClass = clazz;

        while(searchClass != null) {

            Capability annotation = searchClass.getAnnotation(Capability.class);

            if (annotation != null) {
                for (String cap : annotation.value()) {
                    if (cap.equals(capability)) {
                        return true;
                    }
                }
            }

            searchClass = searchClass.getSuperclass();
        }

        return false;
    }
}

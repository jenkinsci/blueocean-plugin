package io.jenkins.blueocean.commons;

import java.util.Collection;

public class ListsUtils {
    public ListsUtils() {
        // no op
    }

    public static <T>T getFirst( Collection<T> collection, T defaultValue){
        if(collection==null){
            return defaultValue;
        }
        return collection.isEmpty()?defaultValue:collection.iterator().next();
    }
}

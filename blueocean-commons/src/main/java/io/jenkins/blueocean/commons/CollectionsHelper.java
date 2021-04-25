package io.jenkins.blueocean.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Some helpers to replace some Guava collections tools
 */
public class CollectionsHelper {
    private CollectionsHelper() {
    }

    public static <K,V> Map<K,V> of(K k1, V v1) {
        Map<K,V> map = new HashMap<>();
        map.put(k1, v1);
        return Collections.unmodifiableMap(map);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2) {
        Map<K,V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K,V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K,V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return Collections.unmodifiableMap(map);
    }

}

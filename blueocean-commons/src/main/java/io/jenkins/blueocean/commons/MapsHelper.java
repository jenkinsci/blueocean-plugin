package io.jenkins.blueocean.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Some helpers to replace some Guava collections tools
 */
public class MapsHelper {
    private MapsHelper() {
    }

    public static <K,V> Map<K,V> of(Map<K,V> m) {
        Map<K,V> map = new HashMap<>(m);
        return Collections.unmodifiableMap(map);
    }

    public static <K,V> Map<K,V> of(K k1, V v1) {
        return Collections.singletonMap(k1,v1);
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

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K,V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return Collections.unmodifiableMap(map);
    }

    public static class Builder<K, V> {
        private HashMap<K,V> map;

        public Builder(){
            map = new HashMap<>();
        }

        public Builder put(K k, V v){
            map.put(k, v);
            return this;
        }

        public Map<K, V> build(){
            return Collections.unmodifiableMap(map);
        }
    }

}

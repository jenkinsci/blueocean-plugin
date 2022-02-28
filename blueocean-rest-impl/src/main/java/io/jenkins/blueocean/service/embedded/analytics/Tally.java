package io.jenkins.blueocean.service.embedded.analytics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Tally {
    private final Map<String, Integer> tally = new HashMap<>();

    /**
     * @param key to increment tally by 1
     */
    public void count(String key) {
        tally.put(key, tally.getOrDefault(key, 0) + 1);
    }

    /**
     * @param key to initialize with zero
     */
    public void zero(String key) {
        tally.putIfAbsent(key, 0);
    }

    /**
     * @return current unmodifiable state of the tally
     */
    public Map<String, Object> get() {
        return Collections.unmodifiableMap(new HashMap<>(tally));
    }
}

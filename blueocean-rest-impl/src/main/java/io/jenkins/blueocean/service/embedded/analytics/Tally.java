package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;

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
     * @return current state of the tally
     */
    public Map<String, Object> get() {
        return ImmutableMap.copyOf(tally);
    }
}

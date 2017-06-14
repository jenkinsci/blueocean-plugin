package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.cache.Cache;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Restricted(NoExternalUse.class)
public class InvalidatingJobCache<T> {

    private final Cache<String, T> cache;

    public InvalidatingJobCache(Cache<String, T> cache) {
        this.cache = cache;
    }

    public T get(String key, Callable<? extends T> valueLoader) {
        try {
            return cache.get(key, valueLoader);
        } catch (ExecutionException e) {
            throw new ServiceException.UnexpectedErrorException("Could not load '" + key + "' from cache", e);
        }
    }

    @Extension
    public class ListenerImpl extends ItemListener {

        @Override
        public void onLocationChanged(Item item, String oldFullName, String newFullName) {
            T cacheValue = cache.getIfPresent(oldFullName);
            if (cacheValue != null) {
                cache.put(newFullName, cacheValue);
            }
        }

        @Override
        public void onDeleted(Item item) {
            cache.invalidate(item.getFullName());
        }
    }
}

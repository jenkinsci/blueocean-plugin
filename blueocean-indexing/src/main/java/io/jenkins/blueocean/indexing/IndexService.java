package io.jenkins.blueocean.indexing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class IndexService {

    private static final Logger LOGGER = Logger.getLogger(IndexService.class.getName());

    // Keep the index open if it has been used in the last 5 minutes otherwise close it to free up space
    final LoadingCache<Item, Index<BlueRun>> runIndexCache = CacheBuilder.<Item, Index<BlueRun>>newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .removalListener(new CacheClosingRemovalListener())
        .build(new IndexCacheLoader());

    public IndexService() {}

    /**
     * Gets an index for the given item
     * @param item to lookup index
     * @return index belonging to the item
     */
    public Index<BlueRun> getIndex(Item item) {
        try {
            return runIndexCache.get(item);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates or opens an index when requested for an item
     */
    static class IndexCacheLoader extends CacheLoader<Item, Index<BlueRun>> {
        @Override
        public Index<BlueRun> load(@Nonnull Item key) throws Exception {
            return Index.open(key.getRootDir());
        }
    }

    /**
     * Closes the index cleanly when the Index has gone unused
     */
    static class CacheClosingRemovalListener implements RemovalListener<Item, Index<BlueRun>> {
        @Override
        public void onRemoval(@Nonnull RemovalNotification<Item, Index<BlueRun>> notification) {
            Index<BlueRun> index = notification.getValue();
            if (index != null) {
                try {
                    index.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Could not close Index <" + index + ">");
                }
            }
        }
    }
}

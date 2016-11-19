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
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
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

    final LoadingCache<Item, Index<BlueRun>> runIndexCache = CacheBuilder.<Item, Index<BlueRun>>newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(new CacheClosingRemovalListener()).build(new IndexCacheLoader());

    public IndexService() {}

    public Index<BlueRun> getIndex(Item item) {
        try {
            return getRunCache().get(item);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static Index<BlueRun> getRunIndex(final Item item) {
        try {
            return getRunCache().get(item);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static class IndexCacheLoader extends CacheLoader<Item, Index<BlueRun>> {
        @Override
        public Index<BlueRun> load(@Nonnull Item key) throws Exception {
            return Index.openRuns(key);
        }
    }

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

    @Extension
    public static class RunListenerImpl extends RunListener<Run<?, ?>> {
        @Override
        public void onFinalized(Run<?, ?> run) {
            Index<BlueRun> index = getRunIndex(run.getParent());
            BlueRun blueRun = getRun(run);
            try {
                index.addDocuments(ImmutableList.of(blueRun), Transformers.RUN_TO_DOCUMENT);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not add run to index", e);
            }
        }

        @Override
        public void onDeleted(Run<?, ?> run) {
            Index<BlueRun> index = getRunIndex(run.getParent());
            try {
                index.delete(Terms.runId(run));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not add run to index", e);
            }
        }
    }

    private static BlueRun getRun(Run<?, ?> run) {
        Reachable parent = BluePipelineFactory.resolve(run.getParent());
        return AbstractRunImpl.getBlueRun(run, parent);
    }

    static LoadingCache<Item, Index<BlueRun>> getRunCache() {
        IndexService indexService = Jenkins.getInstance().getInjector().getProvider(IndexService.class).get();
        return indexService.runIndexCache;
    }
}
